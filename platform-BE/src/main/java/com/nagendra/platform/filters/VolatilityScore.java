package com.nagendra.platform.filters;


import com.nagendra.platform.dto.client.Candles;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VolatilityScore {

  private static final int ATR_PERIOD = 14;
  private static final int RETURN_LOOKBACK = 20; // trading days, ~1 month
  private static final double MAX_SCORE = 10.0; // this component's weight out of 100

  public double calculateVolatilityScore(List<Candles> candles) {

    int minRequired = ATR_PERIOD + 1;
    if (candles.size() < minRequired || candles.size() < RETURN_LOOKBACK + 1) {
      log.warn(
          "Not enough candles for volatility score, need at least {}",
          Math.max(minRequired, RETURN_LOOKBACK + 1));
      return 0;
    }

    candles.sort(Comparator.comparing(Candles::getDate));

    double atr = calculateATR(candles, ATR_PERIOD);
    double latestClose = candles.get(candles.size() - 1).getClose();
    double atrPercent = (atr / latestClose) * 100;

    double returnPercent = calculateReturn(candles, RETURN_LOOKBACK);

    double score = scoreVolatility(returnPercent, atrPercent);

    log.debug(
        "ATR={} ATR%={} Return%={} VolatilityScore={}", atr, atrPercent, returnPercent, score);

    return score;
  }

  // ---------------- ATR (Wilder's smoothing) ----------------

  private double calculateATR(List<Candles> candles, int period) {

    double[] trueRanges = new double[candles.size() - 1];

    for (int i = 1; i < candles.size(); i++) {
      Candles current = candles.get(i);
      Candles prev = candles.get(i - 1);

      double highLow = current.getHigh() - current.getLow();
      double highPrevClose = Math.abs(current.getHigh() - prev.getClose());
      double lowPrevClose = Math.abs(current.getLow() - prev.getClose());

      trueRanges[i - 1] = Math.max(highLow, Math.max(highPrevClose, lowPrevClose));
    }

    double atr = 0;
    for (int i = 0; i < period; i++) {
      atr += trueRanges[i];
    }
    atr /= period;

    for (int i = period; i < trueRanges.length; i++) {
      atr = (atr * (period - 1) + trueRanges[i]) / period;
    }

    return atr;
  }

  // ---------------- Return over lookback ----------------

  private double calculateReturn(List<Candles> candles, int lookback) {
    double startClose = candles.get(candles.size() - 1 - lookback).getClose();
    double endClose = candles.get(candles.size() - 1).getClose();
    return ((endClose - startClose) / startClose) * 100;
  }

  // ---------------- Scoring: reward high return per unit of volatility ----------------

  private double scoreVolatility(double returnPercent, double atrPercent) {

    if (atrPercent <= 0 || returnPercent <= 0) {
      return 0;
    }

    double efficiency = returnPercent / atrPercent;

    double baseScore;
    if (efficiency >= 3.0) {
      baseScore = MAX_SCORE; // 10 - strong return, tightly controlled volatility
    } else if (efficiency >= 2.0) {
      baseScore = MAX_SCORE * 0.7; // 7
    } else if (efficiency >= 1.0) {
      baseScore = MAX_SCORE * 0.5; // 5
    } else {
      baseScore = MAX_SCORE * 0.2; // 2 - return exists but disproportionate volatility
    }

    // Hard cap when ATR% itself is dangerously high, regardless of efficiency.
    if (atrPercent > 7.0) {
      baseScore = Math.min(baseScore, MAX_SCORE * 0.3); // 3
    } else if (atrPercent > 5.0) {
      baseScore = Math.min(baseScore, MAX_SCORE * 0.6); // 6
    }

    return baseScore;
  }

  public Double volatilityScore(
          List<Candles> candles,
          Integer timeDuration) {

    candles.sort(Comparator.comparing(Candles::getDate));

    int period = timeDuration * 5;

    if (candles.size() < period + 1) {
      return 0.0;
    }

    double totalMove = 0;

    for (int i = candles.size() - period;
         i < candles.size();
         i++) {

      if (i <= 0) {
        continue;
      }

      double prev = candles.get(i - 1).getClose();
      double curr = candles.get(i).getClose();

      totalMove += Math.abs(
              ((curr - prev) / prev) * 100.0);
    }

    return totalMove / period;
  }
}
