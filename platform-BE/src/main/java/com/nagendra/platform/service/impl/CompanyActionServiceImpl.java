package com.nagendra.platform.service.impl;

import com.nagendra.platform.client.LivemintClient;
import com.nagendra.platform.client.UpstockClient;
import com.nagendra.platform.dto.client.*;
import com.nagendra.platform.dto.filters.FundamentalScoreRequestDto;
import com.nagendra.platform.enums.StockCategory;
import com.nagendra.platform.filters.*;
import com.nagendra.platform.models.Company;
import com.nagendra.platform.models.MomentumScore;
import com.nagendra.platform.models.Notifications;
import com.nagendra.platform.models.StockCategoryMapping;
import com.nagendra.platform.service.*;
import com.nagendra.platform.utils.CommonUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class CompanyActionServiceImpl implements CompanyActionService {

  private static final DateTimeFormatter PERIOD_FORMATTER =
      DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

  private final LivemintClient livemintClient;

  private final UpstockClient upstockClient;

  private final InstrumentService instrumentService;

  private final CompanyService stockService;

  private final StockCategoryMappingService mappingService;

  private final FundamentalScoreCalculator fundamentalScoreCalc;

  private final PriceReturnScore priceReturnScore;

  private final VolumeScore volumeScore;

  private final TrendScore trendScore;

  private final MomentumIndicator momentumIndicator;

  private final VolatilityScore volatilityScore;

  private final MomentumService momentumService;

  private final NotificationService notificationService;

  @Override
  @Transactional
  @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Kolkata")
  public void addQuarterlyStocks() {

    LocalDate fromDate = LocalDate.now().minusDays(2);
    LocalDate toDate = LocalDate.now().minusDays(2);

    // Fetch today's board meetings
    List<BoardMeetings> meetings = livemintClient.fetchResults(fromDate, toDate);

    // Extract company names
    List<String> companyNames =
        meetings.stream().map(BoardMeetings::getCompanyName).filter(Objects::nonNull).toList();

    // Find instrument keys
    Set<String> instrumentKeys = instrumentService.getInstrumentKeys(companyNames);

    if (instrumentKeys.isEmpty()) {
      log.info("No instrument keys found.");
      return;
    }

    // Fetch market quotes
    MarketQuoteResponse response = upstockClient.getStocksDetailedInfo(instrumentKeys);

    // Filter stocks whose last price is less than 150
    Map<String, Quote> filteredQuotes =
        response.getData().entrySet().stream()
            .filter(
                entry -> {
                  Quote quote = entry.getValue();
                  return quote != null
                      && quote.getLastPrice() != null
                      && quote.getLastPrice() != 0
                      && quote.getLastPrice() < 150;
                })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    List<Company> companies = new ArrayList<>();
    for (Map.Entry<String, Quote> m : filteredQuotes.entrySet()) {
      Company company = new Company();
      company.setIsin(m.getValue().getInstrumentToken());
      company.setStockPrice(BigDecimal.valueOf(m.getValue().getLastPrice()));
      company.setIsMomentumScore(false);
      company.setIsFundamentalScore(false);
      companies.add(company);
    }
    List<Company> result = stockService.saveAll(companies);
    StockCategory stockCategory = StockCategory.QUARTERLY_PERFORMERS;
    List<StockCategoryMapping> categoryMappings =
        result.stream()
            .map(
                company -> {
                  StockCategoryMapping mapping = new StockCategoryMapping();
                  mapping.setCategory(stockCategory);
                  mapping.setStockId(company.getId());
                  return mapping;
                })
            .toList();
    mappingService.saveAll(categoryMappings);
    updateFundamentals(result.stream().map(Company::getIsin).toList());
    Notifications notifications = new Notifications();
    notifications.setMessage("Quarterly stocks added successfully :" + result.size());
    notificationService.saveNotification(notifications);
  }

  private void updateFundamentals(List<String> list) {
    for (String isin : list) {
      updateFundamentalScore(isin);
    }
  }

  @Override
  public void getFundamentalScoreByIsIn(String isin) {
    updateFundamentalScore(isin);
  }

  @Override
  public void getPriceScore(String isin) {
    PriceHistoryResponse priceHistoryResponse = upstockClient.getPriceHistory(isin);
    List<Candles> candles = priceHistoryResponse.getData().getCandles();
    MomentumScore score = momentumService.getMomentumScore(isin);
    Double priceScore = priceReturnScore.calculatePriceReturnScore(candles);
    score.setPriceScore(priceScore);

    Double scoreV = volumeScore.calculateVolumeScore(candles);
    score.setVolumeScore(scoreV);

    Double scoreT = trendScore.calculateTrendScore(candles);
    score.setTrendScore(scoreT);

    Double scoreM = momentumIndicator.calculateMomentumScore(candles);
    score.setMomentumScore(scoreM);

    Double scoreVolatility = volatilityScore.calculateVolatilityScore(candles);
    score.setVolatilityScore(scoreVolatility);
    momentumService.saveMomentumScore(score);
  }

  public void updateFundamentalScore(String instrumentKey) {

    log.info("Instrument key is :{}", instrumentKey);
    String isin = CommonUtils.extractIsin(instrumentKey);

    FundamentalScoreRequestDto dto = new FundamentalScoreRequestDto();

    populateIncomeStatement(dto, upstockClient.getFinancialStatements(isin));

    populateKeyRatios(dto, upstockClient.getKeyRatios(isin));

    populateShareHolding(dto, upstockClient.getHoldingData(isin));

    populateBalanceSheet(dto, upstockClient.getBalanceSheet(isin));

    Integer score = fundamentalScoreCalc.getFundamentalScoreCalculator(dto);
    MomentumScore momentumScore = new MomentumScore();
    momentumScore.setIsin(instrumentKey);
    momentumScore.setFundamentalScore(score);
    momentumService.saveMomentumScore(momentumScore);
  }

  private void populateIncomeStatement(
      FundamentalScoreRequestDto dto, FinancialStatementResponse response) {

    if (response == null
        || response.getData() == null
        || response.getData().getIncomeStatement() == null) {
      return;
    }

    Map<String, IncomeStatement> map =
        response.getData().getIncomeStatement().stream()
            .collect(Collectors.toMap(s -> s.getCategory().toLowerCase(), Function.identity()));

    setGrowth(dto, map.get("revenue"), true);

    setGrowth(dto, map.get("net_profit"), false);
  }

  private void setGrowth(
      FundamentalScoreRequestDto dto, IncomeStatement statement, boolean revenue) {

    if (statement == null) {
      return;
    }

    List<StatementHistory> histories =
        sortByPeriodDesc(statement.getHistory(), StatementHistory::getPeriod);

    if (histories.isEmpty()) {
      return;
    }

    Double value = parsePercentage(histories.get(0).getChange());

    if (revenue) {
      dto.setRevenueGrowthPercentage(value);
    } else {
      dto.setProfitGrowthPercentage(value);
    }
  }

  private void populateKeyRatios(FundamentalScoreRequestDto dto, KeyRatiosResponse response) {

    if (response == null || response.getData() == null) {
      return;
    }

    Map<String, KeyRatio> map =
        response.getData().stream()
            .collect(Collectors.toMap(r -> r.getName().toLowerCase(), Function.identity()));

    KeyRatio roce = map.get("roce");

    if (roce != null) {
      dto.setRoce(parsePercentage(roce.getCompanyValue()));
    }
  }

  private void populateShareHolding(FundamentalScoreRequestDto dto, ShareHoldingResponse response) {

    if (response == null || response.getData() == null) {
      return;
    }

    Map<String, ShareHolding> map =
        response.getData().stream()
            .collect(Collectors.toMap(s -> s.getCategory().toLowerCase(), Function.identity()));

    populateHolding(
        dto,
        map.get("fii"),
        FundamentalScoreRequestDto::setCurrentFii,
        FundamentalScoreRequestDto::setPreviousFii);

    populateHolding(
        dto,
        map.get("other_dii"),
        FundamentalScoreRequestDto::setCurrentDii,
        FundamentalScoreRequestDto::setPreviousDii);
  }

  private void populateHolding(
      FundamentalScoreRequestDto dto,
      ShareHolding holding,
      BiConsumer<FundamentalScoreRequestDto, Double> currentSetter,
      BiConsumer<FundamentalScoreRequestDto, Double> previousSetter) {

    if (holding == null) {
      return;
    }

    List<ShareHoldingHistory> histories =
        sortByPeriodDesc(holding.getHistory(), ShareHoldingHistory::getPeriod);

    if (histories.isEmpty()) {
      return;
    }

    currentSetter.accept(dto, histories.get(0).getValue());

    if (histories.size() > 1) {
      previousSetter.accept(dto, histories.get(1).getValue());
    }
  }

  private void populateBalanceSheet(FundamentalScoreRequestDto dto, BalanceSheetResponse response) {

    if (response == null || response.getData() == null || response.getData().getHistory() == null) {
      return;
    }

    List<BalanceSheetHistory> histories =
        sortByPeriodDesc(response.getData().getHistory(), BalanceSheetHistory::getPeriod);

    if (histories.isEmpty()) {
      return;
    }

    BalanceSheetHistory current = histories.get(0);

    dto.setCurrentAssets(BigDecimal.valueOf(current.getTotalAsset()));
    dto.setCurrentLiabilities(BigDecimal.valueOf(current.getTotalLiability()));

    if (histories.size() > 1) {

      BalanceSheetHistory previous = histories.get(1);

      dto.setPreviousAssets(BigDecimal.valueOf(previous.getTotalAsset()));
      dto.setPreviousLiabilities(BigDecimal.valueOf(previous.getTotalLiability()));
    }
  }

  private <T> List<T> sortByPeriodDesc(List<T> history, Function<T, String> periodExtractor) {

    return history.stream()
        .sorted(
            Comparator.comparing(
                item -> YearMonth.parse(periodExtractor.apply(item).trim(), PERIOD_FORMATTER),
                Comparator.reverseOrder()))
        .toList();
  }

  private Double parsePercentage(String value) {

    if (value == null || value.isBlank()) {
      return null;
    }

    return Double.parseDouble(value.replace("%", "").replace("+", "").replace(",", "").trim());
  }
}
