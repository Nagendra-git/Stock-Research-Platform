package com.nagendra.platform.dto;

import com.nagendra.platform.dto.portfolio.GainLossDto;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class StockStatistics {
  private BigDecimal boughtPrice;
  private BigDecimal soldPrice;
  private BigDecimal currentPrice;
  private Long quantity;

  private GainLossDto bookedPerformance;
  private GainLossDto currentPerformance;
}
