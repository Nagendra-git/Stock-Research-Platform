package com.nagendra.platform.dto.filters;

import lombok.Data;

@Data
public class WeeklyBuyerSellerAnalysis {

  private Double pastThreeDaysPricePerc;

  private Double weeklyPricePerc;
  private Double weeklyVolumePerc;
  private Double weeklyTurnoverPerc;
  private Double weeklyBuyerSellerScore;

  private Double monthlyPricePerc;
  private Double monthlyVolumePerc;
  private Double monthlyTurnoverPerc;
  private Double monthlyBuyerSellerScore;

  private Double threeMonthPricePerc;
  private Double threeMonthTurnoverPerc;
  private Double threeMonthVolumePerc;
  private Double threeMonthBuyerSellerScore;

  private Double sixMonthPricePerc;
  private Double sixMonthBuyerSellerScore;
  private Double sixMonthTurnoverPerc;
  private Double sixMonthVolumePerc;

  private Double swingScore;
}
