package com.nagendra.platform.service.impl;

import com.nagendra.platform.client.UpstockClient;
import com.nagendra.platform.dto.Weekly.BasicResponseDto;
import com.nagendra.platform.dto.client.Candles;
import com.nagendra.platform.dto.client.PriceHistoryResponse;
import com.nagendra.platform.dto.filters.TrendAnalysisResponse;
import com.nagendra.platform.filters.WeeklyMomentumFilter;
import com.nagendra.platform.models.Instrument;
import com.nagendra.platform.models.WeeklyStocks;
import com.nagendra.platform.service.InstrumentService;
import com.nagendra.platform.service.WeeklyMomentumService;
import com.nagendra.platform.service.WeeklyStockService;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyMomentumServiceImpl implements WeeklyMomentumService {
  private static final double MIN_SCORE = 7.0;
  private final UpstockClient upstockClient;
  private final WeeklyMomentumFilter weeklyMomentumFilter;
  private final InstrumentService instrumentService;
  private final WeeklyStockService weeklyStockService;
  Map<String, Double> weeklyMomentumMap = new HashMap<>();

  @Override
  public TrendAnalysisResponse getWeeklyMomentum(String isin, Integer duration) {

    PriceHistoryResponse response = upstockClient.getPriceHistory(isin);

    List<Candles> candles = response.getData().getCandles();
    return weeklyMomentumFilter.calculateWeeklyTrend(isin, candles, duration);
  }

  @Override
  public void addWeeklyMomentum(List<Instrument> instruments) {

    Map<String, List<Candles>> stocks = new HashMap<>();

    log.info("Instruments size is {}", instruments.size());
    for (Instrument instrument : instruments) {

      PriceHistoryResponse response = upstockClient.getPriceHistory(instrument.getInstrumentKey());

      if (response == null
          || response.getData() == null
          || response.getData().getCandles() == null
          || response.getData().getCandles().isEmpty()) {
        continue;
      }

      List<Candles> candles = response.getData().getCandles();

      stocks.put(instrument.getInstrumentKey(), candles);

      // Upstox candles usually come latest first
      instrument.setLastPrice(candles.getFirst().getClose());
    }

    loadMomentumScores(stocks);

    instrumentService.saveAll(instruments);

    weeklyMomentumMap.forEach(
        (isin, score) -> log.info("ISIN: {}, Weekly Momentum Score: {}", isin, score));
  }

  @Override
  public List<BasicResponseDto> getWeeklyMomentumForAll() {
    return weeklyMomentumFilter.getWeeklyStocks();
  }

  @Override
  public void updateWeeklyTrendInMemory(WeeklyStocks stock) {
    if (stock == null || stock.getIsin() == null) {
      log.warn("Stock or ISIN is null. Cannot update weekly trend in memory.");
      return;
    }
    PriceHistoryResponse response = upstockClient.getPriceHistory(stock.getIsin());
    if (response == null
        || response.getData() == null
        || response.getData().getCandles() == null
        || response.getData().getCandles().isEmpty()) {
      return;
    }
    List<Candles> candles = response.getData().getCandles();
    weeklyMomentumFilter.calculateWeeklyTrend(stock.getIsin(), candles, 1);
  }

  public void loadMomentumScores(Map<String, List<Candles>> stocks) {
    weeklyMomentumMap.clear();
    weeklyMomentumMap.putAll(filterTopPerformers(stocks));
    List<WeeklyStocks> weeklyStocks = new ArrayList<>();
    for (Map.Entry<String, Double> entry : weeklyMomentumMap.entrySet()) {
      String isin = entry.getKey();
      Double score = entry.getValue();
      WeeklyStocks weeklyStock = new WeeklyStocks();
      weeklyStock.setIsin(isin);
      weeklyStock.setWeeklyScore(score);
      weeklyStocks.add(weeklyStock);
    }
    weeklyStockService.addAllStocks(weeklyStocks);
    log.info("Saved all well performed stocks: {}", weeklyStocks.size());
    weeklyMomentumMap.clear();
    log.info(
        "Cleared weeklyMomentumMap after saving to database. Current size: {}",
        weeklyMomentumMap.size());
  }

  public Map<String, Double> filterTopPerformers(Map<String, List<Candles>> stocks) {

    return weeklyMomentumFilter.calculateWeeklyMomentumScores(stocks).entrySet().stream()
        .filter(entry -> entry.getValue() >= MIN_SCORE)
        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
  }
}
