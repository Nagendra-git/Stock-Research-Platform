package com.nagendra.platform.dto.marketdata;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PositionGainLoss {

  private String symbol;

  private PnlSummary gainLoss;
}
