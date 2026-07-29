package com.nagendra.platform.dto.filters;

import lombok.Data;

@Data
public class WeeklyTrendScore {
  private String period;
  private Double priceScore;
  private Double volumeScore;
  private Double trendScore;
  private Double momentumScore;
  private Double volatilityScore;
  private Double overallScore;
}
