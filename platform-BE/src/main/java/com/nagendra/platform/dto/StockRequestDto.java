package com.nagendra.platform.dto;

import lombok.Data;

@Data
public class StockRequestDto {
  private String name;

  private String tradingSymbol;

  private String exchange;
}
