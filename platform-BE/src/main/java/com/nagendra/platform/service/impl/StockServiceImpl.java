package com.nagendra.platform.service.impl;

import com.nagendra.platform.client.UpstockClient;
import com.nagendra.platform.dto.AddStockRequestDto;
import com.nagendra.platform.dto.SoldStockDto;
import com.nagendra.platform.dto.StackStatisticsDto;
import com.nagendra.platform.dto.StockStatistics;
import com.nagendra.platform.dto.client.MarketQuoteResponse;
import com.nagendra.platform.dto.client.Quote;
import com.nagendra.platform.enums.StockCategory;
import com.nagendra.platform.models.Company;
import com.nagendra.platform.models.MomentumScore;
import com.nagendra.platform.models.StockCategoryMapping;
import com.nagendra.platform.repository.CompanyRepository;
import com.nagendra.platform.service.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

  private final UpstockClient upstockClient;

  private final CompanyRepository companyRepository;

  private final StockCategoryMappingService mappingService;

  private final InstrumentService instrumentService;

  private final MomentumService momentumService;

  private final StockStatisticsService stockStatisticsService;

  @Override
  @Transactional
  public void addStocks(AddStockRequestDto requestDto) {

    String instrumentKey = instrumentService.getInstrumentKey(requestDto.getStockDetails());
    MarketQuoteResponse response = upstockClient.getStocksDetailedInfo(Set.of(instrumentKey));
    Map<String, Quote> quoteMap =
        response.getData().values().stream()
            .collect(Collectors.toMap(Quote::getInstrumentToken, quote -> quote));
    Quote quote = quoteMap.get(instrumentKey);
    Company company = new Company();
    company.setIsin(instrumentKey);
    company.setStockPrice(BigDecimal.valueOf(quote.getLastPrice()));
    company.setBoughtPrice(requestDto.getStockDetails().getBoughtPrice());
    company.setQuantity(requestDto.getStockDetails().getQuantity());
    company.setSymbol(quote.getSymbol());
    Company result = companyRepository.save(company);
    StockCategory stockCategory = StockCategory.fromString(requestDto.getStockCategory());
    StockCategoryMapping mapping = new StockCategoryMapping();
    mapping.setCategory(stockCategory);
    mapping.setStockId(result.getId());
    mappingService.saveMapping(mapping);
  }

  @Override
  @Transactional
  public void deleteStock(String id) {
    mappingService.removeStockCategory(id);
    companyRepository.deleteById(id);
  }

  @Override
  public MarketQuoteResponse getStockInfo() {
    List<Company> companies = companyRepository.findAll();
    List<StockCategoryMapping> mappings = mappingService.getStockCategoryMappings();
    List<MomentumScore> momentumScores = momentumService.getMomentumScores();
    removeUnnecessaryCompanies(companies, mappings, momentumScores);
    removeDuplicateCompanies(companies, mappings, momentumScores);
    Set<String> instrumentKeys =
        companies.stream().map(Company::getIsin).collect(Collectors.toSet());
    MarketQuoteResponse response = upstockClient.getStocksDetailedInfo(instrumentKeys);
    Map<String, Quote> quoteMap =
        response.getData().values().stream()
            .collect(Collectors.toMap(Quote::getInstrumentToken, quote -> quote));
    for (Company company : companies) {

      String instrumentKey = company.getIsin();

      Quote quote = quoteMap.get(instrumentKey);

      if (quote != null) {
        company.setStockPrice(BigDecimal.valueOf(quote.getLastPrice()));
        company.setSymbol(quote.getSymbol());
      }
    }
    companyRepository.saveAll(companies);

    return response;
  }

  private void removeUnnecessaryCompanies(
      List<Company> companies,
      List<StockCategoryMapping> mappings,
      List<MomentumScore> momentumScores) {

    // Find momentum scores with fundamental score < 15
    List<MomentumScore> scoresToRemove =
        momentumScores.stream()
            .filter(
                score -> score.getFundamentalScore() != null && score.getFundamentalScore() < 15)
            .toList();

    if (scoresToRemove.isEmpty()) {
      return;
    }

    Set<String> invalidIsins =
        scoresToRemove.stream().map(MomentumScore::getIsin).collect(Collectors.toSet());

    // Find companies to remove
    List<Company> companiesToRemove =
        companies.stream().filter(company -> invalidIsins.contains(company.getIsin())).toList();

    Set<String> companyIds =
        companiesToRemove.stream().map(Company::getId).collect(Collectors.toSet());

    // Find mappings to remove
    List<StockCategoryMapping> mappingsToRemove =
        mappings.stream().filter(mapping -> companyIds.contains(mapping.getStockId())).toList();

    // Delete from MongoDB
    companyRepository.deleteAll(companiesToRemove);
    mappingService.deleteAll(mappingsToRemove);
    momentumService.deleteAll(scoresToRemove);

    // Remove from in-memory lists
    companies.removeAll(companiesToRemove);
    mappings.removeAll(mappingsToRemove);
    momentumScores.removeAll(scoresToRemove);
  }

  @Override
  @Transactional
  public void updateStockData(String stockId, SoldStockDto soldStockDto) {
    Company company = getById(stockId);
    company.setSoldPrice(soldStockDto.getSoldPrice());
    company.setQuantity(soldStockDto.getQuantity());
    companyRepository.save(company);
  }

  @Override
  @Transactional
  public List<Company> saveAll(List<Company> companies) {
    return companyRepository.saveAll(companies);
  }

  @Override
  public StockStatistics calculateStats(String stockId) {
    Company company = getById(stockId);
    StackStatisticsDto dto = buildStackStatisticsDto(company);
    return stockStatisticsService.calculateStats(dto);
  }

  @Override
  public List<Company> getMyInvestmentStocks() {
    List<String> stockIds = mappingService.getMyInvestmentStockIds();

    if (stockIds.isEmpty()) {
      return Collections.emptyList();
    }

    return companyRepository.findAllById(stockIds);
  }

  private StackStatisticsDto buildStackStatisticsDto(Company company) {
    return StackStatisticsDto.builder()
        .boughtPrice(company.getBoughtPrice())
        .soldPrice(company.getSoldPrice())
        .currentPrice(company.getStockPrice())
        .quantity(company.getQuantity())
        .build();
  }

  private void removeDuplicateCompanies(
      List<Company> companies,
      List<StockCategoryMapping> mappings,
      List<MomentumScore> momentumScores) {

    Map<String, List<Company>> companiesByIsin =
        companies.stream()
            .filter(company -> company.getIsin() != null)
            .collect(Collectors.groupingBy(company -> getActualIsin(company.getIsin())));

    List<Company> companiesToRemove = new ArrayList<>();

    for (List<Company> companyList : companiesByIsin.values()) {

      if (companyList.size() <= 1) {
        continue;
      }

      boolean hasNse =
          companyList.stream().anyMatch(company -> company.getIsin().startsWith("NSE_EQ|"));

      // If both NSE and BSE exist, remove only BSE
      if (hasNse) {
        companyList.stream()
            .filter(company -> company.getIsin().startsWith("BSE_EQ|"))
            .forEach(companiesToRemove::add);
      }
    }

    if (companiesToRemove.isEmpty()) {
      return;
    }

    Set<String> companyIds =
        companiesToRemove.stream().map(Company::getId).collect(Collectors.toSet());

    Set<String> isinsToRemove =
        companiesToRemove.stream().map(Company::getIsin).collect(Collectors.toSet());

    List<StockCategoryMapping> mappingsToRemove =
        mappings.stream().filter(mapping -> companyIds.contains(mapping.getStockId())).toList();

    List<MomentumScore> momentumScoresToRemove =
        momentumScores.stream().filter(score -> isinsToRemove.contains(score.getIsin())).toList();

    // Delete from MongoDB
    companyRepository.deleteAll(companiesToRemove);
    mappingService.deleteAll(mappingsToRemove);
    momentumService.deleteAll(momentumScoresToRemove);

    // Remove from memory
    companies.removeAll(companiesToRemove);
    mappings.removeAll(mappingsToRemove);
    momentumScores.removeAll(momentumScoresToRemove);
  }

  private String getActualIsin(String isin) {
    if (isin == null) {
      return null;
    }

    int index = isin.indexOf('|');
    return index >= 0 ? isin.substring(index + 1) : isin;
  }

  private Company getById(String id) {
    return companyRepository.findById(id).orElseThrow();
  }
}
