package com.nagendra.platform.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class StockDetailsDto {
  private String tradingSymbol;
  private String exchange;
  private BigDecimal boughtPrice;
  private Long quantity;
}
