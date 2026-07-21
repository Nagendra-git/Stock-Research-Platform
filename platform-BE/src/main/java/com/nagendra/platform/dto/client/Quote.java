package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Quote {

  @JsonProperty("ohlc")
  private Ohlc ohlc;

  @JsonProperty("depth")
  private Depth depth;

  @JsonProperty("timestamp")
  private String timestamp;

  @JsonProperty("instrument_token")
  private String instrumentToken;

  @JsonProperty("symbol")
  private String symbol;

  @JsonProperty("last_price")
  private Double lastPrice;

  @JsonProperty("volume")
  private Long volume;

  @JsonProperty("average_price")
  private Double averagePrice;

  @JsonProperty("oi")
  private Double oi;

  @JsonProperty("net_change")
  private Double netChange;

  @JsonProperty("total_buy_quantity")
  private Double totalBuyQuantity;

  @JsonProperty("total_sell_quantity")
  private Double totalSellQuantity;

  @JsonProperty("lower_circuit_limit")
  private Double lowerCircuitLimit;

  @JsonProperty("upper_circuit_limit")
  private Double upperCircuitLimit;

  @JsonProperty("last_trade_time")
  private String lastTradeTime;

  @JsonProperty("oi_day_high")
  private Double oiDayHigh;

  @JsonProperty("oi_day_low")
  private Double oiDayLow;
}
