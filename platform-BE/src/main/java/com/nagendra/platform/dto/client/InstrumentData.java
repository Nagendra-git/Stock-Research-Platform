package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InstrumentData {

  private String name;

  private String segment;

  private String exchange;

  private String expiry;

  private boolean weekly;

  @JsonProperty("instrument_key")
  private String instrumentKey;

  @JsonProperty("exchange_token")
  private String exchangeToken;

  @JsonProperty("trading_symbol")
  private String tradingSymbol;

  @JsonProperty("tick_size")
  private Double tickSize;

  @JsonProperty("lot_size")
  private Integer lotSize;

  @JsonProperty("instrument_type")
  private String instrumentType;

  @JsonProperty("freeze_quantity")
  private Double freezeQuantity;

  @JsonProperty("underlying_key")
  private String underlyingKey;

  @JsonProperty("underlying_type")
  private String underlyingType;

  @JsonProperty("underlying_symbol")
  private String underlyingSymbol;

  @JsonProperty("strike_price")
  private Double strikePrice;

  @JsonProperty("qty_multiplier")
  private Integer qtyMultiplier;

  @JsonProperty("minimum_lot")
  private Integer minimumLot;
}
