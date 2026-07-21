package com.nagendra.platform.service.impl;

import com.nagendra.platform.client.UpstockClient;
import com.nagendra.platform.dto.AddStockRequestDto;
import com.nagendra.platform.dto.client.InstrumentData;
import com.nagendra.platform.dto.client.InstrumentResponse;
import com.nagendra.platform.enums.StockCategory;
import com.nagendra.platform.mapper.CompanyMapper;
import com.nagendra.platform.models.Company;
import com.nagendra.platform.models.StockCategoryMapping;
import com.nagendra.platform.repository.CompanyRepository;
import com.nagendra.platform.repository.StockCategoryMappingRepository;
import com.nagendra.platform.service.StockService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

  private final UpstockClient upstockClinet;

  private final CompanyRepository companyRepository;

  private final CompanyMapper companyMapper;

  private final StockCategoryMappingRepository mappingRepository;

  @Override
  public void addStocks(AddStockRequestDto requestDto) {

    InstrumentResponse response = upstockClinet.getStocksInfo(requestDto.getNames());
    List<InstrumentData> responseData = response.getData();

    List<Company> companies = responseData.stream().map(companyMapper::toCompany).toList();
    List<Company> result = companyRepository.saveAll(companies);
    StockCategory stockCategory = StockCategory.fromString(requestDto.getStockCategory());
    List<StockCategoryMapping> categoryMappings = result.stream().map(
            company -> {
              StockCategoryMapping mapping = new StockCategoryMapping();
              mapping.setCategory(stockCategory);
              mapping.setId(company.getId());
              return mapping;
            }).toList();
    mappingRepository.saveAll(categoryMappings);
  }
}
