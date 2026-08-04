package com.nagendra.platform.dto.Weekly;

import java.util.List;
import lombok.Data;

@Data
public class BasicResponseDto {
  private String isin;
  private String symbol;
  private String name;
  private Double weeklyPricePercentage;

  private Double weeklyVolumePercentage;

  private Double weeklyTurnoverPercentage;

  private Double buyerSellerScore;

  private Double monthlyPricePercentage;

  private Double monthlyVolumePercentage;

  private Double monthlyTurnoverPercentage;

  private Double monthlyBuyerSellerScore;
  private Double sixMonthPricePercentage;
  private Double sixMonthBuyerSellerScore;
  private Double sixMonthTurnoverPercentage;
  private Double sixMonthVolumePercentage;

  private Double threeMonthPricePercentage;
  private Double threeMonthTurnoverPercentage;
  private Double threeMonthVolumePercentage;
  private Double threeMonthBuyerSellerScore;

  private Double swingScore;
  private Double pastThreeDaysPricePercentage;

  private List<String> tradingCategories;
}
