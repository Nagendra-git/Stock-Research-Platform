package com.nagendra.platform.dto.portfolio;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StockGainLossDto {

  private String symbol;

  private GainLossDto gainLoss;
}
