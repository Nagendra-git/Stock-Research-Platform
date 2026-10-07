package com.nagendra.platform.service.impl;

import com.nagendra.platform.config.UpstoxMarketDataProperties;
import com.nagendra.platform.dto.marketdata.MarketData;
import com.nagendra.platform.exception.MarketDataException;
import com.nagendra.platform.service.MarketDataRedisService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

/**
 * Redis is the single source of truth for "current" market data. Key shape:
 * {redisKeyPrefix}:{yyyy-MM-dd in Asia/Kolkata} Each instrument key is a hash field; each new tick
 * overwrites the previous value for that field. No per-tick history is ever stored.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MarketDataRedisServiceImpl implements MarketDataRedisService {
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  private final UpstoxMarketDataProperties properties;

  @Override
  public String getCurrentRedisKey() {
    LocalDate today = LocalDate.now(ZoneId.of(properties.getMarketData().getTimeZone()));

    return properties.getMarketData().getRedisKeyPrefix() + ":" + today.format(DATE_FMT);
  }

  private String keyFor(LocalDate date) {
    return properties.getMarketData().getRedisKeyPrefix() + ":" + date.format(DATE_FMT);
  }

  @Override
  public void updateMarketData(String instrumentKey, MarketData marketData) {
    try {
      String json = objectMapper.writeValueAsString(marketData);
      String redisKey = getCurrentRedisKey();
      redisTemplate.opsForHash().put(redisKey, instrumentKey, json);
      // TTL as a safety net in addition to explicit daily cleanup
      redisTemplate.expire(redisKey, properties.getMarketData().getRedisTtlHours(), TimeUnit.HOURS);
    } catch (Exception e) {
      log.error("Failed to update Redis market data for {}: {}", instrumentKey, e.getMessage());
    }
  }

  @Override
  public Optional<MarketData> getMarketData(String instrumentKey) {
    try {
      Object raw = redisTemplate.opsForHash().get(getCurrentRedisKey(), instrumentKey);
      if (raw == null) {
        return Optional.empty();
      }
      return Optional.of(objectMapper.readValue(raw.toString(), MarketData.class));
    } catch (Exception e) {
      log.error("Failed to read Redis market data for {}: {}", instrumentKey, e.getMessage());
      return Optional.empty();
    }
  }

  /**
   * Only ever reads TODAY's key (Asia/Kolkata). Never falls back to a previous day's cache - if
   * today has no price yet, returns empty.
   */
  @Override
  public Optional<BigDecimal> getCurrentPrice(String instrumentKey) {
    return getMarketData(instrumentKey).map(MarketData::getLtp);
  }

  @Override
  public Map<String, MarketData> getAllMarketData() {
    try {
      Map<Object, Object> entries = redisTemplate.opsForHash().entries(getCurrentRedisKey());
      return entries.entrySet().stream()
          .collect(
              Collectors.toMap(
                  e -> e.getKey().toString(),
                  e -> {
                    try {
                      return objectMapper.readValue(e.getValue().toString(), MarketData.class);
                    } catch (Exception ex) {
                      throw new MarketDataException("Failed to deserialize market data", ex);
                    }
                  }));
    } catch (Exception e) {
      log.error("Failed to read all Redis market data: {}", e.getMessage());
      return Map.of();
    }
  }

  /**
   * Deletes every market-data:* key EXCEPT today's, so a fresh trading day never sees yesterday's
   * prices. TTL is a backup, not a substitute for this explicit cleanup.
   */
  @Override
  public void clearPreviousMarketData() {
    String todayKey = getCurrentRedisKey();
    String pattern = properties.getMarketData().getRedisKeyPrefix() + ":*";
    Set<String> keys = redisTemplate.keys(pattern);
    if (keys == null || keys.isEmpty()) {
      return;
    }
    keys.remove(todayKey);
    if (!keys.isEmpty()) {
      redisTemplate.delete(keys);
      log.info("Cleared {} stale market-data Redis key(s): {}", keys.size(), keys);
    }
  }

  @Override
  public void initializeTodayCache() {
    String todayKey = getCurrentRedisKey();
    Boolean exists = redisTemplate.hasKey(todayKey);
    if (Boolean.TRUE.equals(exists)) {
      log.info("Today's market-data cache already exists: {}", todayKey);
      return;
    }
    // Creating an empty hash: Redis has no explicit "create empty hash"
    // op, so we just let the first HSET create it lazily. Log intent
    // for observability.
    log.info("Initialized today's market-data cache key: {}", todayKey);
  }
}
