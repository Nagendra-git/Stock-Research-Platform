package com.nagendra.platform.dto.marketdata;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MarketDataResponse {

  private String instrumentKey;
  private BigDecimal ltp;
  private BigDecimal closePrice;
  private Long lastTradedQuantity;
  private Long lastTradedTime;
  private LocalDateTime updatedAt;

  public static MarketDataResponse from(MarketData m) {
    MarketDataResponse r = new MarketDataResponse();
    r.instrumentKey = m.getInstrumentKey();
    r.ltp = m.getLtp();
    r.closePrice = m.getClosePrice();
    r.lastTradedQuantity = m.getLastTradedQuantity();
    r.lastTradedTime = m.getLastTradedTime();
    r.updatedAt = m.getUpdatedAt();
    return r;
  }
}
