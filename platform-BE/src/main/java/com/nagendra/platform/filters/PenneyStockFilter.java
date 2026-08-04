package com.nagendra.platform.filters;

import com.nagendra.platform.dto.client.Candles;
import com.nagendra.platform.dto.filters.WeeklyBuyerSellerAnalysis;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PenneyStockFilter {

  private final PriceReturnScore priceReturnScore;

  private final VolumeScore volumeScore;

  private final TurnOverFilter turnOverFilter;

  public WeeklyBuyerSellerAnalysis analyzeWeeklyMomentum(List<Candles> candles) {

    WeeklyBuyerSellerAnalysis analysis = new WeeklyBuyerSellerAnalysis();

    Double pricePercentage = priceReturnScore.priceStrength(candles, 5);

    Double volumePercentage = volumeScore.weeklyVolumeStrength(candles);
    Double sixMonthPricePercentage = priceReturnScore.priceStrength(candles, 126);
    Double sixMonthVolumePercentage = volumeScore.volumeStrength(candles, 126, 126);
    Double sixMonthTurnoverPercentage = turnOverFilter.turnoverStrength(candles, 126, 126);

    Double threeMonthPricePercentage = priceReturnScore.priceStrength(candles, 63);
    Double threeMonthVolumePercentage = volumeScore.volumeStrength(candles, 63, 63);
    Double threeMonthTurnoverPercentage = turnOverFilter.turnoverStrength(candles, 63, 63);
    Double threeMonthBuyerSellerScore =
        buyerSellerPressureScore(
            threeMonthPricePercentage, threeMonthVolumePercentage, threeMonthTurnoverPercentage);
    analysis.setThreeMonthPricePerc(threeMonthPricePercentage);
    analysis.setThreeMonthVolumePerc(threeMonthVolumePercentage);
    analysis.setThreeMonthTurnoverPerc(threeMonthTurnoverPercentage);
    analysis.setThreeMonthBuyerSellerScore(threeMonthBuyerSellerScore);
    Double turnoverPercentage = turnOverFilter.weeklyTurnoverStrength(candles);

    Double monthlyPricePercentage = priceReturnScore.priceStrength(candles, 20);

    Double monthlyVolumePercentage =
        volumeScore.weeklyVolumeStrength(candles.subList(0, Math.min(candles.size(), 20)));

    Double monthlyTurnoverPercentage =
        turnOverFilter.weeklyTurnoverStrength(candles.subList(0, Math.min(candles.size(), 20)));

    Double weeklyBuyerSellerScore =
        buyerSellerPressureScore(pricePercentage, volumePercentage, turnoverPercentage);
    analysis.setWeeklyPricePerc(pricePercentage);
    analysis.setWeeklyVolumePerc(volumePercentage);
    analysis.setWeeklyTurnoverPerc(turnoverPercentage);
    analysis.setWeeklyBuyerSellerScore(weeklyBuyerSellerScore);
    analysis.setMonthlyPricePerc(monthlyPricePercentage);
    analysis.setMonthlyVolumePerc(monthlyVolumePercentage);
    analysis.setMonthlyTurnoverPerc(monthlyTurnoverPercentage);
    Double monthlyBuyerSellerScore =
        buyerSellerPressureScore(
            monthlyPricePercentage, monthlyVolumePercentage, monthlyTurnoverPercentage);
    analysis.setMonthlyBuyerSellerScore(monthlyBuyerSellerScore);
    Double pastThreeDaysPricePercentage = priceReturnScore.priceStrength(candles, 3);
    analysis.setPastThreeDaysPricePerc(pastThreeDaysPricePercentage);
    analysis.setSixMonthPricePerc(sixMonthPricePercentage);
    analysis.setSixMonthVolumePerc(sixMonthVolumePercentage);
    analysis.setSixMonthTurnoverPerc(sixMonthTurnoverPercentage);
    analysis.setSixMonthBuyerSellerScore(
        buyerSellerPressureScore(
            sixMonthPricePercentage, sixMonthVolumePercentage, sixMonthTurnoverPercentage));
    Double swingScore =
        calculateSwingScore(
            sixMonthPricePercentage,
            monthlyBuyerSellerScore,
            weeklyBuyerSellerScore,
            pastThreeDaysPricePercentage);
    analysis.setSwingScore(swingScore);
    return analysis;
  }

  private Double calculateSwingScore(
      Double sixMonthPricePercentage,
      Double monthlyBuyerSellerScore,
      Double weeklyBuyerSellerScore,
      Double pastThreeDaysPricePercentage) {
    return (normalizePrice(sixMonthPricePercentage) * 0.30)
        + ((10 - monthlyBuyerSellerScore) * 0.20)
        + (weeklyBuyerSellerScore * 0.30)
        + (normalizePrice(pastThreeDaysPricePercentage) * 0.20);
  }

  private Double buyerSellerPressureScore(
      Double pricePerc, Double volumePerc, Double turnoverPerc) {

    double priceScore = normalizePrice(pricePerc);

    double volumeScore = normalizeVolume(volumePerc);

    double turnoverScore = normalizeTurnover(turnoverPerc);

    return (priceScore * 0.50) + (volumeScore * 0.25) + (turnoverScore * 0.25);
  }

  private double normalizePrice(double pricePerc) {

    if (pricePerc >= 20) return 10;

    if (pricePerc <= -20) return 0;

    return ((pricePerc + 20) / 40.0) * 10;
  }

  private double normalizeVolume(double volumePerc) {

    if (volumePerc >= 100) return 10;

    if (volumePerc <= -100) return 0;

    return ((volumePerc + 100) / 200.0) * 10;
  }

  private double normalizeTurnover(double turnoverPerc) {

    if (turnoverPerc >= 100) return 10;

    if (turnoverPerc <= -100) return 0;

    return ((turnoverPerc + 100) / 200.0) * 10;
  }
}
