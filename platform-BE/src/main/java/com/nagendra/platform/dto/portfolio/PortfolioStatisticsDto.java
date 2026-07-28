package com.nagendra.platform.dto.portfolio;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortfolioStatisticsDto {

  private long totalStocks;
  private long holdingStocks;
  private long soldStocks;
  private long gainStocks;
  private long lossStocks;
}
