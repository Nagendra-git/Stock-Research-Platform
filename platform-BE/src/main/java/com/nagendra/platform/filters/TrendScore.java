package com.nagendra.platform.filters;

import com.nagendra.platform.dto.client.Candles;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TrendScore {

  public Double calculateTrendScore(List<Candles> candles) {

    if (candles.size() < 50) {
      return 0.0;
    }

    candles.sort(Comparator.comparing(Candles::getDate));

    double currentPrice = candles.get(candles.size() - 1).getClose();

    double dma20 = calculateDMA(candles, 20);
    double dma50 = calculateDMA(candles, 50);

    // Strong Trend
    if (candles.size() >= 200) {

      double dma200 = calculateDMA(candles, 200);

      if (currentPrice > dma50 && dma50 > dma200) {
        return 15.0;
      }
    }

    // Emerging Trend
    if (currentPrice > dma50 && currentPrice > dma20) {
      return 12.0;
    }

    // Early Momentum
    if (currentPrice > dma20) {
      return 8.0;
    }

    // Weak Trend
    return 0.0;
  }

  private double calculateDMA(List<Candles> candles, int period) {

    if (candles.size() < period) {
      throw new IllegalArgumentException("Not enough candles for DMA " + period);
    }

    return candles.subList(candles.size() - period, candles.size()).stream()
        .mapToDouble(Candles::getClose)
        .average()
        .orElse(0.0);
  }
}
