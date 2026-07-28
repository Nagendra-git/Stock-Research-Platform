package com.nagendra.platform.dto.portfolio;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PortfolioSummaryDto {

  private BigDecimal investment;
  private BigDecimal currentValue;

  private GainLossDto overall;
  private GainLossDto booked;
  private GainLossDto holding;
}
