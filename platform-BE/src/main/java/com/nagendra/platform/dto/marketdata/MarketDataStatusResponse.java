package com.nagendra.platform.dto.marketdata;

import lombok.Data;

@Data
public class MarketDataStatusResponse {

  private boolean enabled;
  private boolean connected;
  private boolean marketOpen;
  private String marketDate;
  private String redisKey;
  private int subscribedInstruments;

  public MarketDataStatusResponse(
      boolean enabled,
      boolean connected,
      boolean marketOpen,
      String marketDate,
      String redisKey,
      int subscribedInstruments) {
    this.enabled = enabled;
    this.connected = connected;
    this.marketOpen = marketOpen;
    this.marketDate = marketDate;
    this.redisKey = redisKey;
    this.subscribedInstruments = subscribedInstruments;
  }
}
