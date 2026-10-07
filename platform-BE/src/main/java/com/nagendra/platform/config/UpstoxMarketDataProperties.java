package com.nagendra.platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.LocalTime;

@ConfigurationProperties(prefix = "upstox")
public class UpstoxMarketDataProperties {

  /** Upstox API access token. Injected via UPSTOX_ACCESS_TOKEN env var - never logged. */
  private String accessToken;

  @NestedConfigurationProperty private MarketData marketData = new MarketData();

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public MarketData getMarketData() {
    return marketData;
  }

  public void setMarketData(MarketData marketData) {
    this.marketData = marketData;
  }

  public static class MarketData {
    private boolean enabled = true;
    private String authorizeUrl = "https://api.upstox.com/v3/feed/market-data-feed/authorize";
    private LocalTime marketStart = LocalTime.of(9, 15);
    private LocalTime marketEnd = LocalTime.of(15, 30);
    private String timeZone = "Asia/Kolkata";
    private String subscriptionMode = "full";
    private String redisKeyPrefix = "market-data";
    private long redisTtlHours = 24;
    private Reconnect reconnect = new Reconnect();
    private long instrumentRefreshIntervalSeconds = 60;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public String getAuthorizeUrl() {
      return authorizeUrl;
    }

    public void setAuthorizeUrl(String authorizeUrl) {
      this.authorizeUrl = authorizeUrl;
    }

    public LocalTime getMarketStart() {
      return marketStart;
    }

    public void setMarketStart(LocalTime marketStart) {
      this.marketStart = marketStart;
    }

    public LocalTime getMarketEnd() {
      return marketEnd;
    }

    public void setMarketEnd(LocalTime marketEnd) {
      this.marketEnd = marketEnd;
    }

    public String getTimeZone() {
      return timeZone;
    }

    public void setTimeZone(String timeZone) {
      this.timeZone = timeZone;
    }

    public String getSubscriptionMode() {
      return subscriptionMode;
    }

    public void setSubscriptionMode(String subscriptionMode) {
      this.subscriptionMode = subscriptionMode;
    }

    public String getRedisKeyPrefix() {
      return redisKeyPrefix;
    }

    public void setRedisKeyPrefix(String redisKeyPrefix) {
      this.redisKeyPrefix = redisKeyPrefix;
    }

    public long getRedisTtlHours() {
      return redisTtlHours;
    }

    public void setRedisTtlHours(long redisTtlHours) {
      this.redisTtlHours = redisTtlHours;
    }

    public Reconnect getReconnect() {
      return reconnect;
    }

    public void setReconnect(Reconnect reconnect) {
      this.reconnect = reconnect;
    }

    public long getInstrumentRefreshIntervalSeconds() {
      return instrumentRefreshIntervalSeconds;
    }

    public void setInstrumentRefreshIntervalSeconds(long v) {
      this.instrumentRefreshIntervalSeconds = v;
    }

    public static class Reconnect {
      private long initialDelayMs = 2000;
      private long maxDelayMs = 30000;

      public long getInitialDelayMs() {
        return initialDelayMs;
      }

      public void setInitialDelayMs(long initialDelayMs) {
        this.initialDelayMs = initialDelayMs;
      }

      public long getMaxDelayMs() {
        return maxDelayMs;
      }

      public void setMaxDelayMs(long maxDelayMs) {
        this.maxDelayMs = maxDelayMs;
      }
    }
  }
}
