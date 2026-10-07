package com.nagendra.platform.service.impl;

import com.nagendra.platform.client.UpstoxWebSocketClient;
import com.nagendra.platform.config.UpstoxMarketDataProperties;
import com.nagendra.platform.dto.marketdata.MarketData;
import com.nagendra.platform.service.MarketDataRedisService;
import com.nagendra.platform.service.MarketInstrumentService;
import com.nagendra.platform.service.UpstoxMarketDataManager;
import com.nagendra.platform.trading.PositionMonitorService;
import com.nagendra.platform.upstox.ProtoFeedMapper;
import com.nagendra.platform.upstox.UpstoxAuthService;
import com.nagendra.platform.upstox.UpstoxSubscriptionService;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/**
 * Owns the single Upstox WebSocket connection for the whole application. Responsible for: authorize
 * -> connect -> subscribe -> parse -> Redis, plus reconnection with exponential backoff and
 * duplicate-connection prevention. Runs independently of any controller.
 */
@Slf4j
@Service
@Repository
@RequiredArgsConstructor
public class UpstoxMarketDataManagerImpl implements UpstoxMarketDataManager {

  private final UpstoxAuthService authService;
  private final UpstoxSubscriptionService subscriptionService;
  private final ProtoFeedMapper feedMapper;
  private final MarketDataRedisService redisService;
  private final MarketInstrumentService instrumentService;
  private final UpstoxMarketDataProperties properties;
  private final PositionMonitorService positionMonitorService;

  private final AtomicBoolean connecting = new AtomicBoolean(false);
  private final AtomicBoolean connected = new AtomicBoolean(false);
  private final AtomicBoolean stopping = new AtomicBoolean(false);
  private final AtomicReference<UpstoxWebSocketClient> activeClient = new AtomicReference<>();
  private final Set<String> currentSubscriptions = ConcurrentHashMap.newKeySet();

  private final ScheduledExecutorService reconnectExecutor =
      Executors.newSingleThreadScheduledExecutor(
          r -> {
            Thread t = new Thread(r, "upstox-reconnect");
            t.setDaemon(true);
            return t;
          });

  private volatile long currentBackoffMs;

  /** Idempotent - safe to call even if already connected. Prevents duplicate connections. */
  @Override
  public void start() {
    if (!properties.getMarketData().isEnabled()) {
      log.info("Upstox market data feed is disabled via configuration");
      return;
    }
    if (connected.get() || connecting.get()) {
      log.info("Upstox market data manager already running - ignoring duplicate start()");
      return;
    }

    List<String> instrumentKeys = instrumentService.getInstrumentKeys();
    if (instrumentKeys.isEmpty()) {
      log.warn(
          "No instruments to subscribe to - not connecting. Will retry on next scheduler tick.");
      return;
    }

    redisService.clearPreviousMarketData();
    redisService.initializeTodayCache();
    currentSubscriptions.clear();
    currentSubscriptions.addAll(instrumentKeys);
    connectInternal();
  }

  @Override
  public void stop() {
    stopping.set(true);
    UpstoxWebSocketClient client = activeClient.getAndSet(null);
    if (client != null) {
      client.close();
    }
    connected.set(false);
    connecting.set(false);
    log.info("Upstox market data manager stopped");
  }

  @Override
  public boolean isConnected() {
    return connected.get();
  }

  @Override
  public int getSubscribedInstrumentCount() {
    return currentSubscriptions.size();
  }

  /**
   * Reloads instruments from MongoDB and diff-subscribes/unsubscribes without tearing down the
   * WebSocket connection.
   */
  @Override
  public void refreshSubscriptions() {
    if (!connected.get()) {
      log.debug("refreshSubscriptions() called while disconnected - skipping");
      return;
    }
    List<String> latest = instrumentService.getInstrumentKeys();
    Set<String> latestSet = Set.copyOf(latest);

    Set<String> toAdd = new java.util.HashSet<>(latestSet);
    toAdd.removeAll(currentSubscriptions);

    Set<String> toRemove = new java.util.HashSet<>(currentSubscriptions);
    toRemove.removeAll(latestSet);

    UpstoxWebSocketClient client = activeClient.get();
    if (client == null) {
      return;
    }

    if (!toAdd.isEmpty()) {
      byte[] msg =
          subscriptionService.buildSubscribeMessage(
              List.copyOf(toAdd), properties.getMarketData().getSubscriptionMode());
      client.sendBinary(msg);
      currentSubscriptions.addAll(toAdd);
      log.info("Subscribed {} new instrument(s)", toAdd.size());
    }
    if (!toRemove.isEmpty()) {
      byte[] msg = subscriptionService.buildUnsubscribeMessage(List.copyOf(toRemove));
      client.sendBinary(msg);
      currentSubscriptions.removeAll(toRemove);
      log.info("Unsubscribed {} removed instrument(s)", toRemove.size());
    }
  }

  private void connectInternal() {
    if (!connecting.compareAndSet(false, true)) {
      return; // another thread is already connecting
    }
    stopping.set(false);

    try {
      String wsUrl = authService.fetchAuthorizedWebSocketUrl();
      UpstoxWebSocketClient client = new UpstoxWebSocketClient();
      activeClient.set(client);

      client.connect(wsUrl, () -> onOpen(client), this::onFeed, this::onClosed, this::onFailure);
    } catch (Exception e) {
      connecting.set(false);
      log.error("Failed to establish Upstox WebSocket connection: {}", e.getMessage());
      scheduleReconnect();
    }
  }

  private void onOpen(UpstoxWebSocketClient client) {
    connected.set(true);
    connecting.set(false);
    currentBackoffMs =
        properties.getMarketData().getReconnect().getInitialDelayMs(); // reset backoff
    byte[] subscribeMsg =
        subscriptionService.buildSubscribeMessage(
            List.copyOf(currentSubscriptions), properties.getMarketData().getSubscriptionMode());
    client.sendBinary(subscribeMsg);
    log.info(
        "Subscribed to {} instruments in '{}' mode",
        currentSubscriptions.size(),
        properties.getMarketData().getSubscriptionMode());
  }

  private void onFeed(com.upstox.marketdatafeeder.rpc.proto.FeedResponse response) {
    try {
      var marketDataByInstrument = feedMapper.map(response);
      for (var entry : marketDataByInstrument.entrySet()) {
        MarketData md = entry.getValue();
        redisService.updateMarketData(entry.getKey(), md);
        // Automated stop-loss/target watcher: cheap in-memory check per tick,
        // any actual exit order is dispatched to its own thread inside here.
        positionMonitorService.onTick(entry.getKey(), md);
      }
    } catch (Exception e) {
      log.error("Error processing feed tick: {}", e.getMessage());
    }
  }

  private void onClosed() {
    connected.set(false);
    activeClient.set(null);
    if (!stopping.get()) {
      scheduleReconnect();
    }
  }

  private void onFailure(Throwable t) {
    connected.set(false);
    connecting.set(false);
    activeClient.set(null);
    if (!stopping.get()) {
      scheduleReconnect();
    }
  }

  private void scheduleReconnect() {
    if (stopping.get()) {
      return;
    }
    long delay = currentBackoffMs;
    long maxDelay = properties.getMarketData().getReconnect().getMaxDelayMs();
    currentBackoffMs = Math.min(maxDelay, currentBackoffMs * 2);

    log.info("Scheduling Upstox WebSocket reconnect in {} ms", delay);
    reconnectExecutor.schedule(
        () -> {
          if (!stopping.get()) {
            // Always obtains a NEW authorized_redirect_uri - never reuses the old one
            connectInternal();
          }
        },
        delay,
        TimeUnit.MILLISECONDS);
  }

  @PreDestroy
  public void shutdown() {
    stop();
    reconnectExecutor.shutdownNow();
  }
}
