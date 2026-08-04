package com.nagendra.platform.service.impl;

import com.nagendra.platform.client.UpstockClient;
import com.nagendra.platform.dto.Weekly.BasicResponseDto;
import com.nagendra.platform.dto.Weekly.WeeklyMomentumPageResponse;
import com.nagendra.platform.dto.client.Candles;
import com.nagendra.platform.dto.client.PriceHistoryResponse;
import com.nagendra.platform.dto.filters.TrendAnalysisResponse;
import com.nagendra.platform.dto.filters.WeeklyBuyerSellerAnalysis;
import com.nagendra.platform.filters.PenneyStockFilter;
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
  private static final double MIN_SCORE = 8.5;
  private final UpstockClient upstockClient;
  private final WeeklyMomentumFilter weeklyMomentumFilter;
  private final InstrumentService instrumentService;
  private final WeeklyStockService weeklyStockService;
  private final PenneyStockFilter penneyStockFilter;
  Map<String, WeeklyBuyerSellerAnalysis> weeklyMomentumMap = new HashMap<>();

  private static WeeklyStocks getWeeklyStocks(
      Map.Entry<String, WeeklyBuyerSellerAnalysis> entry,
      List<String> categories,
      WeeklyBuyerSellerAnalysis analysis) {
    WeeklyStocks weeklyStock = new WeeklyStocks();

    weeklyStock.setIsin(entry.getKey());

    weeklyStock.setTradingCategories(categories);

    weeklyStock.setBuyerSellerScore(analysis.getWeeklyBuyerSellerScore());

    weeklyStock.setWeeklyPricePercentage(analysis.getWeeklyPricePerc());

    weeklyStock.setWeeklyVolumePercentage(analysis.getWeeklyVolumePerc());

    weeklyStock.setWeeklyTurnoverPercentage(analysis.getWeeklyTurnoverPerc());

    weeklyStock.setMonthlyBuyerSellerScore(analysis.getMonthlyBuyerSellerScore());

    weeklyStock.setMonthlyPricePercentage(analysis.getMonthlyPricePerc());

    weeklyStock.setSixMonthBuyerSellerScore(analysis.getSixMonthBuyerSellerScore());

    weeklyStock.setSixMonthPricePercentage(analysis.getSixMonthPricePerc());
    weeklyStock.setSixMonthVolumePercentage(analysis.getSixMonthVolumePerc());
    weeklyStock.setSixMonthTurnoverPercentage(analysis.getSixMonthTurnoverPerc());
    weeklyStock.setPastThreeDaysPricePercentage(analysis.getPastThreeDaysPricePerc());
    weeklyStock.setMonthlyTurnoverPercentage(analysis.getMonthlyTurnoverPerc());
    weeklyStock.setMonthlyVolumePercentage(analysis.getMonthlyVolumePerc());
    weeklyStock.setThreeMonthBuyerSellerScore(analysis.getThreeMonthBuyerSellerScore());
    weeklyStock.setThreeMonthPricePercentage(analysis.getThreeMonthPricePerc());
    weeklyStock.setThreeMonthVolumePercentage(analysis.getThreeMonthVolumePerc());
    weeklyStock.setThreeMonthTurnoverPercentage(analysis.getThreeMonthTurnoverPerc());
    weeklyStock.setSwingScore(analysis.getSwingScore());
    return weeklyStock;
  }

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

    filterPenneyStocks(stocks);

    instrumentService.saveAll(instruments);
  }

  private void filterPenneyStocks(Map<String, List<Candles>> stocks) {

    weeklyMomentumMap.clear();

    weeklyMomentumMap.putAll(filterTopPerformers(stocks));

    List<WeeklyStocks> weeklyStocks = new ArrayList<>();

    for (Map.Entry<String, WeeklyBuyerSellerAnalysis> entry : weeklyMomentumMap.entrySet()) {

      WeeklyBuyerSellerAnalysis analysis = entry.getValue();

      List<String> categories = new ArrayList<>();

      if (isLongTermCandidate(analysis)) {
        categories.add("LONG_TERM");
      }

      if (isSwingCandidate(analysis)) {
        categories.add("SWING");
      }

      if (isShortTermCandidate(analysis)) {
        categories.add("SHORT_TERM");
      }

      if (categories.isEmpty()) {
        continue;
      }

      WeeklyStocks weeklyStock = getWeeklyStocks(entry, categories, analysis);

      weeklyStocks.add(weeklyStock);
    }

    weeklyStockService.addAllStocks(weeklyStocks);

    log.info("Saved stocks: {}", weeklyStocks.size());
  }

  private boolean isShortTermCandidate(WeeklyBuyerSellerAnalysis stock) {

    return stock.getPastThreeDaysPricePerc() > 5
        && stock.getWeeklyPricePerc() > 8
        && stock.getWeeklyBuyerSellerScore() >= 7.5
        && stock.getWeeklyVolumePerc() > 50
        && stock.getWeeklyTurnoverPerc() > 50;
  }

  private boolean isSwingCandidate(WeeklyBuyerSellerAnalysis stock) {

    return stock.getSixMonthPricePerc() > 20
        && stock.getMonthlyPricePerc() < 5
        && stock.getMonthlyPricePerc() > -20
        && stock.getWeeklyBuyerSellerScore() >= 6
        && stock.getPastThreeDaysPricePerc() > 3;
  }

  private boolean isLongTermCandidate(WeeklyBuyerSellerAnalysis stock) {

    return stock.getSixMonthPricePerc() > 30
        && stock.getThreeMonthPricePerc() > 10
        && stock.getSixMonthBuyerSellerScore() >= 7
        && stock.getSixMonthVolumePerc() > 0
        && stock.getSixMonthTurnoverPerc() > 0;
  }

  @Override
  public WeeklyMomentumPageResponse getWeeklyMomentumForAll(
      int page, int size, String sortBy, String direction) {

    List<WeeklyStocks> weeklyStocks = weeklyStockService.getAllStocks();

    Comparator<WeeklyStocks> comparator = getComparator(sortBy);

    if ("desc".equalsIgnoreCase(direction)) {
      comparator = comparator.reversed();
    }

    List<WeeklyStocks> sortedStocks = weeklyStocks.stream().sorted(comparator).toList();

    long totalElements = sortedStocks.size();
    int totalPages = (int) Math.ceil((double) totalElements / size);

    int fromIndex = page * size;

    if (fromIndex >= totalElements) {
      return new WeeklyMomentumPageResponse(
          Collections.emptyList(), page, size, totalElements, totalPages, page == 0, true);
    }

    int toIndex = Math.min(fromIndex + size, sortedStocks.size());

    List<WeeklyStocks> pagedStocks = sortedStocks.subList(fromIndex, toIndex);

    Set<String> isins = pagedStocks.stream().map(WeeklyStocks::getIsin).collect(Collectors.toSet());

    Map<String, Instrument> instruments = instrumentService.getAllInstrumentsByIsIn(isins);

    List<BasicResponseDto> content = buildResponse(pagedStocks, instruments);

    return new WeeklyMomentumPageResponse(
        content, page, size, totalElements, totalPages, page == 0, page + 1 >= totalPages);
  }

  private List<BasicResponseDto> buildResponse(
      List<WeeklyStocks> weeklyStocks, Map<String, Instrument> instruments) {

    return weeklyStocks.stream()
        .map(
            weeklyStock -> {
              BasicResponseDto dto = new BasicResponseDto();

              dto.setIsin(weeklyStock.getIsin());

              Instrument instrument = instruments.get(weeklyStock.getIsin());

              if (instrument != null) {
                dto.setSymbol(instrument.getTradingSymbol());
                dto.setName(instrument.getName());
              }

              // Weekly
              dto.setWeeklyPricePercentage(weeklyStock.getWeeklyPricePercentage());
              dto.setWeeklyVolumePercentage(weeklyStock.getWeeklyVolumePercentage());
              dto.setWeeklyTurnoverPercentage(weeklyStock.getWeeklyTurnoverPercentage());
              dto.setBuyerSellerScore(weeklyStock.getBuyerSellerScore());

              // Monthly
              dto.setMonthlyTurnoverPercentage(weeklyStock.getMonthlyTurnoverPercentage());
              dto.setMonthlyVolumePercentage(weeklyStock.getMonthlyVolumePercentage());
              dto.setMonthlyPricePercentage(weeklyStock.getMonthlyPricePercentage());
              dto.setMonthlyBuyerSellerScore(weeklyStock.getMonthlyBuyerSellerScore());

              // Six Month
              dto.setSixMonthPricePercentage(weeklyStock.getSixMonthPricePercentage());
              dto.setSixMonthBuyerSellerScore(weeklyStock.getSixMonthBuyerSellerScore());
              dto.setSixMonthTurnoverPercentage(weeklyStock.getSixMonthTurnoverPercentage());
              dto.setSixMonthVolumePercentage(weeklyStock.getSixMonthVolumePercentage());

              // Three Month
              dto.setThreeMonthPricePercentage(weeklyStock.getThreeMonthPricePercentage());
              dto.setThreeMonthTurnoverPercentage(weeklyStock.getThreeMonthTurnoverPercentage());
              dto.setThreeMonthVolumePercentage(weeklyStock.getThreeMonthVolumePercentage());
              dto.setThreeMonthBuyerSellerScore(weeklyStock.getThreeMonthBuyerSellerScore());

              // Swing
              dto.setSwingScore(weeklyStock.getSwingScore());

              // Short term
              dto.setPastThreeDaysPricePercentage(weeklyStock.getPastThreeDaysPricePercentage());

              // Categories
              dto.setTradingCategories(weeklyStock.getTradingCategories());

              return dto;
            })
        .collect(Collectors.toList());
  }

  @Override
  public WeeklyBuyerSellerAnalysis addWeeklyAnalysis(String isin) {
    Map<String, Instrument> instrument = instrumentService.getAllInstrumentsByIsIn(Set.of(isin));
    List<Instrument> instruments = new ArrayList<>();
    for (Map.Entry<String, Instrument> entry : instrument.entrySet()) {
      instruments.add(entry.getValue());
    }
    addWeeklyMomentum(instruments);
    return weeklyMomentumMap.get(isin);
  }

  public Map<String, WeeklyBuyerSellerAnalysis> filterTopPerformers(
      Map<String, List<Candles>> stocks) {

    for (Map.Entry<String, List<Candles>> entry : stocks.entrySet()) {
      String isin = entry.getKey();
      List<Candles> candles = entry.getValue();

      if (candles == null || candles.size() < 20) {
        continue;
      }

      WeeklyBuyerSellerAnalysis analysis = penneyStockFilter.analyzeWeeklyMomentum(candles);
      weeklyMomentumMap.put(isin, analysis);
    }
    return weeklyMomentumMap;
  }

  private Comparator<WeeklyStocks> getComparator(String sortBy) {

    return switch (sortBy) {
      case "monthlyBuyerSellerScore" ->
          Comparator.comparing(
              WeeklyStocks::getMonthlyBuyerSellerScore, Comparator.nullsLast(Double::compareTo));

      case "sixMonthBuyerSellerScore" ->
          Comparator.comparing(
              WeeklyStocks::getSixMonthBuyerSellerScore, Comparator.nullsLast(Double::compareTo));

      case "threeMonthBuyerSellerScore" ->
          Comparator.comparing(
              WeeklyStocks::getThreeMonthBuyerSellerScore, Comparator.nullsLast(Double::compareTo));

      case "weeklyBuyerSellerScore" ->
          Comparator.comparing(
              WeeklyStocks::getBuyerSellerScore, Comparator.nullsLast(Double::compareTo));

      case "pastThreeDaysPricePercentage" ->
          Comparator.comparing(
              WeeklyStocks::getPastThreeDaysPricePercentage,
              Comparator.nullsLast(Double::compareTo));

      default ->
          Comparator.comparing(
              WeeklyStocks::getSwingScore, Comparator.nullsLast(Double::compareTo));
    };
  }
}
