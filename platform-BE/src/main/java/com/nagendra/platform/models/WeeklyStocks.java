package com.nagendra.platform.models;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("weekly_stocks")
@EqualsAndHashCode(callSuper = false)
public class WeeklyStocks extends Audit {

  @Id private String id;

  private String isin;

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
