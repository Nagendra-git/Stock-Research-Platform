package com.nagendra.platform.service;

import com.nagendra.platform.dto.marketdata.MarketData;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

public interface MarketDataRedisService {

  /** Returns today's Redis key based on the configured time zone. */
  String getCurrentRedisKey();

  /** Stores the latest market data for an instrument. */
  void updateMarketData(String instrumentKey, MarketData marketData);

  /** Returns the latest market data for an instrument. */
  Optional<MarketData> getMarketData(String instrumentKey);

  /** Returns the latest LTP for an instrument. */
  Optional<BigDecimal> getCurrentPrice(String instrumentKey);

  /** Returns all market data stored for today. */
  Map<String, MarketData> getAllMarketData();

  /** Removes market-data Redis keys belonging to previous days. */
  void clearPreviousMarketData();

  /** Initializes today's market-data cache. */
  void initializeTodayCache();
}
