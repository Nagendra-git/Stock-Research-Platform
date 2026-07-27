package com.nagendra.platform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InstrumentDto {

  @JsonProperty("instrument_key")
  private String instrumentKey;

  @JsonProperty("exchange_token")
  private String exchangeToken;

  @JsonProperty("tradingsymbol")
  private String tradingSymbol;

  private String name;

  @JsonProperty("last_price")
  private Double lastPrice;

  @JsonProperty("tick_size")
  private Double tickSize;

  @JsonProperty("lot_size")
  private Integer lotSize;

  @JsonProperty("instrument_type")
  private String instrumentType;

  private String exchange;

  private String isin;
}
