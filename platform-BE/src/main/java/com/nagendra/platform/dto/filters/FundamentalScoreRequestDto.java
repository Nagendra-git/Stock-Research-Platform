package com.nagendra.platform.dto.filters;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class FundamentalScoreRequestDto {
  private Double revenueGrowthPercentage;
  private Double profitGrowthPercentage;
  private Double roce;
  private Double currentFii;
  private Double previousFii;

  private Double currentDii;
  private Double previousDii;

  private BigDecimal currentAssets;
  private BigDecimal previousAssets;

  private BigDecimal currentLiabilities;
  private BigDecimal previousLiabilities;
}
