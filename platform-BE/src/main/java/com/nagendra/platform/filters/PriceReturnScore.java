package com.nagendra.platform.filters;

import static com.nagendra.platform.constants.UpstoxResourceProfileConstants.WEEK_WEIGHTS;

import com.nagendra.platform.dto.client.Candles;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PriceReturnScore {

  public double calculatePriceReturnScore(List<Candles> candles) {

    candles.sort(Comparator.comparing(Candles::getDate));

    Candles latest = candles.getLast();

    double current = latest.getClose();

    double r1w =
        calculateReturn(
            current, findNearestCandle(candles, latest.getDate().minusWeeks(1)).getClose());
    double r1m =
        calculateReturn(
            current, findNearestCandle(candles, latest.getDate().minusMonths(1)).getClose());
    double r3m =
        calculateReturn(
            current, findNearestCandle(candles, latest.getDate().minusMonths(3)).getClose());
    double r6m =
        calculateReturn(
            current, findNearestCandle(candles, latest.getDate().minusMonths(6)).getClose());
    double rawScore = (r1w * 0.10) + (r1m * 0.08) + (r3m * 0.05) + (r6m * 0.02);

    return normalizeTo25(rawScore);
  }

  private Candles findNearestCandle(List<Candles> candles, LocalDate targetDate) {

    // First try previous trading day
    Optional<Candles> previous =
        candles.stream()
            .filter(candle -> !candle.getDate().isAfter(targetDate))
            .max(Comparator.comparing(Candles::getDate));

    return previous.orElseGet(
        () ->
            candles.stream()
                .min(Comparator.comparing(Candles::getDate))
                .orElseThrow(() -> new IllegalArgumentException("No candle data available")));

    // If no previous candle exists, take next available trading day
  }

  private double calculateReturn(double currentPrice, double historicalPrice) {

    if (historicalPrice <= 0) {
      throw new IllegalArgumentException("Historical price must be greater than zero");
    }

    return ((currentPrice - historicalPrice) / historicalPrice) * 100.0;
  }

  private double normalizeTo25(double rawScore) {

    double min = -25.0;
    double max = 25.0;

    double bounded = Math.clamp(rawScore, min, max);

    return ((bounded - min) / (max - min)) * 25.0;
  }

  public Double priceReturnScore(List<Candles> candles, Integer timeDuration) {

    candles.sort(Comparator.comparing(Candles::getDate));

    if (candles.size() < 6) {
      return 0.0;
    }

    double current = candles.getLast().getClose();
    double score = 0.0;

    for (int week = 0; week < Math.min(timeDuration, WEEK_WEIGHTS.size()); week++) {

      score += calculateWeekScore(current, candles, WEEK_WEIGHTS.get(week), (week * 5) + 1);
    }

    return score;
  }

  private double calculateWeekScore(
      double current, List<Candles> candles, double[] weights, int offset) {

    double score = 0.0;

    for (int day = 0; day < weights.length; day++) {

      int candleIndex = candles.size() - 1 - offset - day;

      if (candleIndex < 0) {
        break;
      }

      double previous = candles.get(candleIndex).getClose();

      score += calculateReturn(current, previous) * weights[day];
    }

    return score;
  }

  public Double priceStrength(List<Candles> candles, int period) {

    candles.sort(Comparator.comparing(Candles::getDate));

    if (candles.size() <= period) {
      return 0.0;
    }

    double currentPrice = candles.getLast().getClose();

    double averagePrice =
        candles.subList(candles.size() - period - 1, candles.size() - 1).stream()
            .mapToDouble(Candles::getClose)
            .average()
            .orElse(0);

    if (averagePrice == 0) {
      return 0.0;
    }

    return ((currentPrice - averagePrice) / averagePrice) * 100;
  }


}
