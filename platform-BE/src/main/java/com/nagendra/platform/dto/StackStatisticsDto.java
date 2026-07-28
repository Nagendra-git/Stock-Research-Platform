package com.nagendra.platform.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StackStatisticsDto {
  private BigDecimal boughtPrice;
  private BigDecimal soldPrice;
  private BigDecimal currentPrice;
  private Long quantity;
}
