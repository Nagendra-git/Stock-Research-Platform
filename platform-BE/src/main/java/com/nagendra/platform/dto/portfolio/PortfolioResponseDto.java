package com.nagendra.platform.dto.portfolio;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PortfolioResponseDto {

  private PortfolioSummaryDto portfolioSummary;

  private PortfolioStatisticsDto statistics;

  private List<StockGainLossDto> stocks;
}
