package com.nagendra.platform.filters;

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
}
