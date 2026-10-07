package com.nagendra.platform.trading;

import com.nagendra.platform.dto.marketdata.MarketData;
import com.nagendra.platform.enums.ExitReason;
import com.nagendra.platform.models.Position;
import com.nagendra.platform.service.PositionService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * This is the "watch the feed and auto-exit" piece. UpstoxMarketDataManager calls onTick(...) for
 * every instrument on every incoming WebSocket tick (see the call added in onFeed()); this class
 * checks that tick's price against every OPEN position on that instrument and fires an exit the
 * moment stop-loss or target is crossed.
 *
 * <p>Exit orders are placed on a separate single-thread executor so a slow order-placement HTTP
 * call never blocks the WebSocket feed thread, and so at most one exit is ever in flight per
 * position at a time (dedup'd via pendingExits).
 */
@Service
public class PositionMonitorService {

  private static final Logger log = LoggerFactory.getLogger(PositionMonitorService.class);

  private final PositionService positionService;
  private final Set<String> pendingExits = ConcurrentHashMap.newKeySet();
  private final ExecutorService exitExecutor =
      Executors.newSingleThreadExecutor(
          r -> {
            Thread t = new Thread(r, "position-exit-worker");
            t.setDaemon(true);
            return t;
          });
  // Trailing-stop moves are persisted off the feed thread too, on their own
  // executor so a burst of exits never delays writing a ratcheted stop.
  private final ExecutorService trailingStopExecutor =
      Executors.newSingleThreadExecutor(
          r -> {
            Thread t = new Thread(r, "trailing-stop-writer");
            t.setDaemon(true);
            return t;
          });

  public PositionMonitorService(PositionService positionService) {
    this.positionService = positionService;
  }

  public void onTick(String instrumentKey, MarketData marketData) {
    if (marketData == null || marketData.getLtp() == null) {
      return;
    }
    List<Position> positions = positionService.getOpenPositionsForInstrument(instrumentKey);
    if (positions.isEmpty()) {
      return;
    }
    BigDecimal ltp = marketData.getLtp();

    for (Position position : positions) {
      evaluate(position, ltp);
    }
  }

  private void evaluate(Position position, BigDecimal ltp) {
    if (position.isTrailingStopEnabled()) {
      applyTrailingStop(position, ltp);
    }

    ExitReason reason = null;
    if (ltp.compareTo(position.getStopLossPrice()) <= 0) {
      reason =
          position.isTrailingStopEnabled()
              ? ExitReason.TRAILING_STOP_HIT
              : ExitReason.STOP_LOSS_HIT;
    } else if (ltp.compareTo(position.getTargetPrice()) >= 0) {
      reason = ExitReason.TARGET_HIT;
    }

    if (reason == null) {
      return;
    }

    // Dedup: rapid consecutive ticks can both see the trigger before the
    // first exit finishes and the position leaves the cache.
    if (!pendingExits.add(position.getId())) {
      return;
    }

    ExitReason finalReason = reason;
    log.info(
        "Position {} on {} triggered {} at ltp={} (sl={}, target={})",
        position.getId(),
        position.getInstrumentKey(),
        finalReason,
        ltp,
        position.getStopLossPrice(),
        position.getTargetPrice());

    exitExecutor.submit(
        () -> {
          try {
            positionService.exitPosition(position.getId(), ltp, finalReason);
          } catch (Exception e) {
            log.error(
                "Failed to execute triggered exit for position {}: {}",
                position.getId(),
                e.getMessage(),
                e);
          } finally {
            pendingExits.remove(position.getId());
          }
        });
  }

  /**
   * Ratchets the stop-loss up as price makes new highs, but never moves it down. Runs on the WS
   * feed thread so must stay cheap - it's just BigDecimal comparisons, no I/O.
   */
  private void applyTrailingStop(Position position, BigDecimal ltp) {
    if (position.getHighestPriceSinceEntry() == null
        || ltp.compareTo(position.getHighestPriceSinceEntry()) > 0) {
      position.setHighestPriceSinceEntry(ltp);
      BigDecimal newStop =
          ltp.multiply(
                  BigDecimal.ONE.subtract(
                      position
                          .getTrailingStopPercent()
                          .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)))
              .setScale(2, RoundingMode.HALF_UP);
      if (newStop.compareTo(position.getStopLossPrice()) > 0) {
        // Update the in-memory (cached) object immediately so this same tick's
        // threshold check below sees the new stop, then persist off-thread.
        position.setStopLossPrice(newStop);
        String positionId = position.getId();
        BigDecimal persistedHigh = position.getHighestPriceSinceEntry();
        trailingStopExecutor.submit(
            () -> positionService.persistTrailingStopUpdate(positionId, newStop, persistedHigh));
      }
    }
  }
}
