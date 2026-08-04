package com.nagendra.platform.filters;

import com.nagendra.platform.dto.client.Candles;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VolumeScore {

  public double calculateVolumeScore(List<Candles> candles) {

    if (candles.size() < 21) {
      return 0;
    }

    candles.sort(Comparator.comparing(Candles::getDate));

    Candles latest = candles.get(candles.size() - 1);

    long currentVolume = latest.getVolume();

    List<Candles> previous20Days = candles.subList(candles.size() - 21, candles.size() - 1);

    double averageVolume =
        previous20Days.stream().mapToLong(Candles::getVolume).average().orElse(0);

    double ratio = currentVolume / averageVolume;

    return calculateVolumeScore(ratio);
  }

  private int calculateVolumeScore(double ratio) {
    if (ratio >= 3.0) {
      return 15;
    } else if (ratio >= 2.0) {
      return 10;
    } else if (ratio >= 1.0) {
      return 5;
    }
    return 0;
  }

  public Double volumeScore(List<Candles> candles, Integer timeDuration) {

    candles.sort(Comparator.comparing(Candles::getDate));

    int recentDays = timeDuration * 5;

    if (candles.size() < recentDays * 2) {
      return 0.0;
    }

    long recentVolume =
        candles.subList(candles.size() - recentDays, candles.size()).stream()
            .mapToLong(Candles::getVolume)
            .sum();

    long previousVolume =
        candles.subList(candles.size() - (recentDays * 2), candles.size() - recentDays).stream()
            .mapToLong(Candles::getVolume)
            .sum();

    return ((double) (recentVolume - previousVolume) / previousVolume) * 100;
  }

  public Double volumeStrength(List<Candles> candles, int recentPeriod, int averagePeriod) {

    candles.sort(Comparator.comparing(Candles::getDate));

    if (candles.size() < recentPeriod + averagePeriod) {
      return 0.0;
    }

    double recentAverageVolume =
        candles.subList(candles.size() - recentPeriod, candles.size()).stream()
            .mapToLong(Candles::getVolume)
            .average()
            .orElse(0);

    double previousAverageVolume =
        candles
            .subList(candles.size() - recentPeriod - averagePeriod, candles.size() - recentPeriod)
            .stream()
            .mapToLong(Candles::getVolume)
            .average()
            .orElse(0);

    if (previousAverageVolume == 0) {
      return 0.0;
    }

    return ((recentAverageVolume - previousAverageVolume) / previousAverageVolume) * 100;
  }

  public Double weeklyVolumeStrength(List<Candles> candles) {

    candles.sort(Comparator.comparing(Candles::getDate));

    int period = 5;

    if (candles.size() < period * 2) {
      return 0.0;
    }

    double currentWeekVolume =
        candles.subList(candles.size() - period, candles.size()).stream()
            .mapToLong(Candles::getVolume)
            .average()
            .orElse(0);

    double previousWeekVolume =
        candles.subList(candles.size() - (period * 2), candles.size() - period).stream()
            .mapToLong(Candles::getVolume)
            .average()
            .orElse(0);

    if (previousWeekVolume == 0) {
      return 0.0;
    }

    return ((currentWeekVolume - previousWeekVolume) / previousWeekVolume) * 100;
  }
}
