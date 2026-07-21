package com.nagendra.platform.service.impl;

import com.nagendra.platform.client.UpstockClient;
import com.nagendra.platform.dto.AddStockRequestDto;
import com.nagendra.platform.dto.BoughtStockDto;
import com.nagendra.platform.dto.client.InstrumentData;
import com.nagendra.platform.dto.client.InstrumentResponse;
import com.nagendra.platform.dto.client.MarketQuoteResponse;
import com.nagendra.platform.dto.client.Quote;
import com.nagendra.platform.enums.StockCategory;
import com.nagendra.platform.mapper.CompanyMapper;
import com.nagendra.platform.models.Company;
import com.nagendra.platform.models.StockCategoryMapping;
import com.nagendra.platform.repository.CompanyRepository;
import com.nagendra.platform.service.StockCategoryMappingService;
import com.nagendra.platform.service.StockService;
import java.util.List;
import java.util.Map;
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

  private final CompanyMapper companyMapper;

  private final StockCategoryMappingService mappingService;

  @Override
  @Transactional
  public void addStocks(AddStockRequestDto requestDto) {

    InstrumentResponse response = upstockClient.getStocksInfo(requestDto.getStocks());
    List<InstrumentData> responseData = response.getData();

    List<Company> companies = responseData.stream().map(companyMapper::toCompany).toList();
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
    List<String> instrumentKeys = companies.stream().map(Company::getInstrumentKey).toList();
    MarketQuoteResponse response = upstockClient.getStocksDetailedInfo(instrumentKeys);
    Map<String, Quote> quoteMap =
        response.getData().values().stream()
            .collect(Collectors.toMap(Quote::getInstrumentToken, quote -> quote));
    for (Company company : companies) {

      String instrumentKey = company.getInstrumentKey();

      Quote quote = quoteMap.get(instrumentKey);

      if (quote != null) {
        company.setStockPrice(quote.getLastPrice());
      }
    }
    companyRepository.saveAll(companies);

    return response;
  }

  @Override
  @Transactional
  public void updateStockData(String stockId, BoughtStockDto boughtStockDto) {
    Company company = getById(stockId);
    company.setBoughtPrice(boughtStockDto.getBoughtPrice());
    company.setQuantity(boughtStockDto.getQuantity());
    companyRepository.save(company);
  }

  private Company getById(String id) {
    return companyRepository.findById(id).orElseThrow();
  }
}
