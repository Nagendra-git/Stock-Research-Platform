package com.nagendra.platform.filters;

import com.nagendra.platform.dto.Weekly.BasicResponseDto;
import com.nagendra.platform.dto.client.Candles;
import com.nagendra.platform.dto.filters.TrendAnalysisResponse;
import com.nagendra.platform.dto.filters.WeeklyTrendScore;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyMomentumFilter {

  private final PriceReturnScore priceReturnScore;
  private final VolumeScore volumeScore;
  private final TrendScore trendScore;
  private final MomentumIndicator momentumIndicator;
  private final VolatilityScore volatilityScore;

  Map<String, TrendAnalysisResponse> weeklyMap = new HashMap<>();

  public Map<String, Double> calculateWeeklyMomentumScores(Map<String, List<Candles>> stocks) {

    Map<String, Double> scores = new HashMap<>();

    for (Map.Entry<String, List<Candles>> entry : stocks.entrySet()) {

      String isin = entry.getKey();
      List<Candles> candles = entry.getValue();

      if (candles == null || candles.size() < 30) {
        continue;
      }

      double score = calculateWeeklyMomentumScore(candles);

      scores.put(isin, score);
    }

    return scores;
  }

  public TrendAnalysisResponse calculateWeeklyTrend(
      String isin, List<Candles> candles, Integer timeDuration) {

    TrendAnalysisResponse response = new TrendAnalysisResponse();
    response.setIsin(isin);

    response.setWeeklyScores(calculateTrend(candles, timeDuration));
    response.setMonthlyScores(calculateTrend(candles, 4));
    weeklyMap.put(isin, response);
    return response;
  }

  private WeeklyTrendScore calculateTrend(List<Candles> candles, Integer timeDuration) {

    WeeklyTrendScore score = new WeeklyTrendScore();
    Double priceScore = priceReturnScore.priceReturnScore(candles, timeDuration);

    Double volumeScoreValue = volumeScore.volumeScore(candles, timeDuration);

    Double trendScoreValue = trendScore.trendScore(candles, timeDuration);

    Double momentumScoreValue = momentumIndicator.momentumScore(candles, timeDuration);

    Double volatilityScoreValue = volatilityScore.volatilityScore(candles, timeDuration);

    score.setPriceScore(priceScore);
    score.setVolumeScore(volumeScoreValue);
    score.setTrendScore(trendScoreValue);
    score.setMomentumScore(momentumScoreValue);
    score.setVolatilityScore(volatilityScoreValue);

    // Overall weekly ranking score
    double overall =
        (priceScore * 0.30)
            + (volumeScoreValue * 0.20)
            + (trendScoreValue * 0.20)
            + (momentumScoreValue * 0.20)
            + (volatilityScoreValue * 0.10);

    score.setOverallScore(overall);

    return score;
  }

  public double calculateWeeklyMomentumScore(List<Candles> candles) {

    candles.sort(Comparator.comparing(Candles::getDate));

    Candles latest = candles.getLast();
    double current = latest.getClose();

    double d1 = calculateReturn(current, candles.get(candles.size() - 2).getClose());

    double d2 = calculateReturn(current, candles.get(candles.size() - 3).getClose());

    double d3 = calculateReturn(current, candles.get(candles.size() - 4).getClose());

    double d4 = calculateReturn(current, candles.get(candles.size() - 5).getClose());

    double d5 = calculateReturn(current, candles.get(candles.size() - 6).getClose());

    return (d1 * 0.35) + (d2 * 0.25) + (d3 * 0.20) + (d4 * 0.12) + (d5 * 0.08);
  }

  private double calculateReturn(double current, double previous) {
    return ((current - previous) / previous) * 100.0;
  }

  public List<BasicResponseDto> getWeeklyStocks() {

    return weeklyMap.values().stream()
        .filter(
            response -> response.getWeeklyScores() != null && response.getMonthlyScores() != null)
        .sorted(
            Comparator.comparingDouble(
                    (TrendAnalysisResponse response) ->
                        response.getWeeklyScores().getOverallScore())
                .reversed()
                .thenComparingDouble(response -> response.getMonthlyScores().getOverallScore()))
        .map(
            response -> {
              BasicResponseDto dto = new BasicResponseDto();
              dto.setIsin(response.getIsin());
              dto.setWeeklyScore(response.getWeeklyScores().getOverallScore());
              dto.setMonthlyScore(response.getMonthlyScores().getOverallScore());
              return dto;
            })
        .toList();
  }
}
