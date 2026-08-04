package com.nagendra.platform.filters;

import com.nagendra.platform.dto.client.Candles;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TurnOverFilter {

  public Double turnoverStrength(List<Candles> candles, int recentPeriod, int baselinePeriod) {

    candles.sort(Comparator.comparing(Candles::getDate));

    if (candles.size() < recentPeriod + baselinePeriod) {
      return 0.0;
    }

    double recentTurnover =
        candles.subList(candles.size() - recentPeriod, candles.size()).stream()
            .mapToDouble(c -> c.getClose() * c.getVolume())
            .average()
            .orElse(0);

    double previousTurnover =
        candles
            .subList(candles.size() - recentPeriod - baselinePeriod, candles.size() - recentPeriod)
            .stream()
            .mapToDouble(c -> c.getClose() * c.getVolume())
            .average()
            .orElse(0);

    if (previousTurnover == 0) {
      return 0.0;
    }

    return ((recentTurnover - previousTurnover) / previousTurnover) * 100;
  }

  public Double averageTurnover(List<Candles> candles, int period) {

    candles.sort(Comparator.comparing(Candles::getDate));

    if (candles.size() < period) {
      return 0.0;
    }

    return candles.subList(candles.size() - period, candles.size()).stream()
        .mapToDouble(c -> c.getClose() * c.getVolume())
        .average()
        .orElse(0);
  }

  public Double calculateDailyTurnover(Candles candle) {

    return candle.getClose() * candle.getVolume();
  }

  public Double weeklyTurnoverStrength(List<Candles> candles) {

    candles.sort(Comparator.comparing(Candles::getDate));

    int period = 5;

    if (candles.size() < period * 2) {
      return 0.0;
    }

    double currentWeekTurnover =
        candles.subList(candles.size() - period, candles.size()).stream()
            .mapToDouble(c -> c.getClose() * c.getVolume())
            .average()
            .orElse(0);

    double previousWeekTurnover =
        candles.subList(candles.size() - (period * 2), candles.size() - period).stream()
            .mapToDouble(c -> c.getClose() * c.getVolume())
            .average()
            .orElse(0);

    if (previousWeekTurnover == 0) {
      return 0.0;
    }

    return ((currentWeekTurnover - previousWeekTurnover) / previousWeekTurnover) * 100;
  }
}
