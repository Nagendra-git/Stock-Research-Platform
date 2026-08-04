package com.nagendra.platform.dto.filters;

import lombok.Data;

@Data
public class WeeklyTrendScore {

  private Double priceScore;
  private Double volumeScore;

  private Double weeklyPricePerc;
  private Double monthlyPricePerc;

  private Double weeklyVolumePerc;
  private Double monthlyVolumePerc;

  private Double trendScore;
  private Double momentumScore;
  private Double volatilityScore;

  private Double buyingStrengthScore;

  private Double overallScore;

  private Double weeklyTurnoverPerc;
  private Double monthlyTurnoverPerc;
  private Double averageDailyTurnover;
}
