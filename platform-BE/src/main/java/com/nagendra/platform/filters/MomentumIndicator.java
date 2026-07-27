package com.nagendra.platform.filters;

import com.nagendra.platform.dto.client.Candles;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MomentumIndicator {

  private static final int RSI_PERIOD = 14;
  private static final int MACD_FAST_PERIOD = 12;
  private static final int MACD_SLOW_PERIOD = 26;
  private static final int MACD_SIGNAL_PERIOD = 9;

  private static final double RSI_MAX_SCORE = 5.0;
  private static final double MACD_MAX_SCORE = 5.0;

  public double calculateMomentumScore(List<Candles> candles) {

    int minRequired = MACD_SLOW_PERIOD + MACD_SIGNAL_PERIOD;
    if (candles.size() < minRequired) {
      log.warn(
          "Not enough candles for momentum score, need at least {}, got {}",
          minRequired,
          candles.size());
      return 0;
    }

    // Defensive: always force ascending order, regardless of input order.
    List<Candles> sorted = candles.stream().sorted(Comparator.comparing(Candles::getDate)).toList();

    LocalDate firstDate = sorted.get(0).getDate();
    LocalDate lastDate = sorted.get(sorted.size() - 1).getDate();

    if (firstDate.isAfter(lastDate)) {
      log.error("Sort order still wrong after sorting - check Candles.date parsing");
    }

    double rsi = calculateRSI(sorted, RSI_PERIOD);
    double rsiScore = scoreRSI(rsi);

    double[] macdAndSignal = calculateMACD(sorted);
    double macdLine = macdAndSignal[0];
    double signalLine = macdAndSignal[1];
    double macdScore = scoreMACD(macdLine, signalLine);

    return rsiScore + macdScore;
  }

  // ---------------- RSI (Wilder's smoothing) ----------------

  private double calculateRSI(List<Candles> candles, int period) {

    if (candles.size() < period + 1) {
      return 50;
    }

    double avgGain = 0;
    double avgLoss = 0;

    for (int i = 1; i <= period; i++) {
      double change = candles.get(i).getClose() - candles.get(i - 1).getClose();
      if (change > 0) {
        avgGain += change;
      } else {
        avgLoss += -change;
      }
    }
    avgGain /= period;
    avgLoss /= period;

    for (int i = period + 1; i < candles.size(); i++) {
      double change = candles.get(i).getClose() - candles.get(i - 1).getClose();
      double gain = change > 0 ? change : 0;
      double loss = change < 0 ? -change : 0;

      avgGain = (avgGain * (period - 1) + gain) / period;
      avgLoss = (avgLoss * (period - 1) + loss) / period;
    }

    if (avgLoss == 0) {
      return 100;
    }

    double rs = avgGain / avgLoss;
    return 100 - (100 / (1 + rs));
  }

  private double scoreRSI(double rsi) {
    if (rsi >= 50 && rsi <= 70) {
      return RSI_MAX_SCORE;
    } else if (rsi > 70) {
      return RSI_MAX_SCORE * 0.4;
    } else if (rsi >= 40) {
      return RSI_MAX_SCORE * 0.6;
    }
    return 0;
  }

  // ---------------- MACD ----------------

  private double[] calculateMACD(List<Candles> candles) {

    double[] closes = candles.stream().mapToDouble(Candles::getClose).toArray();

    double[] emaFast = ema(closes, MACD_FAST_PERIOD);
    double[] emaSlow = ema(closes, MACD_SLOW_PERIOD);

    int startIdx = MACD_SLOW_PERIOD - 1;
    double[] macdSeries = new double[closes.length - startIdx];

    for (int i = startIdx; i < closes.length; i++) {
      macdSeries[i - startIdx] = emaFast[i] - emaSlow[i];
    }

    double[] signalSeries = ema(macdSeries, MACD_SIGNAL_PERIOD);

    double latestMacd = macdSeries[macdSeries.length - 1];
    double latestSignal = signalSeries[signalSeries.length - 1];

    return new double[] {latestMacd, latestSignal};
  }

  private double scoreMACD(double macdLine, double signalLine) {
    if (macdLine > signalLine && macdLine > 0) {
      return MACD_MAX_SCORE; // 5 - genuine bullish momentum
    } else if (macdLine > signalLine) {
      return MACD_MAX_SCORE * 0.4; // 2 - crossover but still below zero (weak/bounce)
    }
    return 0;
  }

  // Returns the full EMA series (values before index period-1 are 0/unused)
  private double[] ema(double[] values, int period) {

    double[] result = new double[values.length];

    if (values.length < period) {
      return result;
    }

    double multiplier = 2.0 / (period + 1);

    double sma = 0;
    for (int i = 0; i < period; i++) {
      sma += values[i];
    }
    sma /= period;
    result[period - 1] = sma;

    for (int i = period; i < values.length; i++) {
      result[i] = (values[i] - result[i - 1]) * multiplier + result[i - 1];
    }

    return result;
  }
}
