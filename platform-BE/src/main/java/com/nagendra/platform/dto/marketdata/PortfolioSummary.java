package com.nagendra.platform.dto.marketdata;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PortfolioSummary {

  private PnlSummary booked;

  private BigDecimal currentValue;

  private PnlSummary holding;

  private BigDecimal investment;

  private PnlSummary overall;
}
