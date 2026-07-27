package com.nagendra.platform.service.impl;

import com.nagendra.platform.client.UpstockClient;
import com.nagendra.platform.dto.AddStockRequestDto;
import com.nagendra.platform.dto.BoughtStockDto;
import com.nagendra.platform.dto.client.MarketQuoteResponse;
import com.nagendra.platform.dto.client.Quote;
import com.nagendra.platform.enums.StockCategory;
import com.nagendra.platform.models.Company;
import com.nagendra.platform.models.MomentumScore;
import com.nagendra.platform.models.StockCategoryMapping;
import com.nagendra.platform.repository.CompanyRepository;
import com.nagendra.platform.service.InstrumentService;
import com.nagendra.platform.service.MomentumService;
import com.nagendra.platform.service.StockCategoryMappingService;
import com.nagendra.platform.service.StockService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  @Override
  @Transactional
  public void addStocks(AddStockRequestDto requestDto) {

    Set<String> instrumentKeys = instrumentService.getInstrumentKeys(requestDto.getCompanyNames());
    MarketQuoteResponse response = upstockClient.getStocksDetailedInfo(instrumentKeys);
    Map<String, Quote> quoteMap =
        response.getData().values().stream()
            .collect(Collectors.toMap(Quote::getInstrumentToken, quote -> quote));
    List<Company> companies = new ArrayList<>();
    for (String key : instrumentKeys) {
      Quote quote = quoteMap.get(key);
      Company company = new Company();
      company.setIsin(key);
      company.setStockPrice(quote.getLastPrice());
      companies.add(company);
    }
    List<Company> result = companyRepository.saveAll(companies);
    StockCategory stockCategory = StockCategory.fromString(requestDto.getStockCategory());
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
        company.setStockPrice(quote.getLastPrice());
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
  public void updateStockData(String stockId, BoughtStockDto boughtStockDto) {
    Company company = getById(stockId);
    company.setBoughtPrice(boughtStockDto.getBoughtPrice());
    company.setQuantity(boughtStockDto.getQuantity());
    companyRepository.save(company);
  }

  @Override
  @Transactional
  public List<Company> saveAll(List<Company> companies) {

    return companyRepository.saveAll(companies);
  }

  private Company getById(String id) {
    return companyRepository.findById(id).orElseThrow();
  }
}
