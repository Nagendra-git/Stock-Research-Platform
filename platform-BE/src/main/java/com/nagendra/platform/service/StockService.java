package com.nagendra.platform.service;

import com.nagendra.platform.dto.AddStockRequestDto;
import com.nagendra.platform.dto.SoldStockDto;
import com.nagendra.platform.dto.StockStatistics;
import com.nagendra.platform.dto.client.MarketQuoteResponse;
import com.nagendra.platform.models.Company;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface StockService {
  void addStocks(AddStockRequestDto requestDto);

  void deleteStock(String id);

  MarketQuoteResponse getStockInfo();

  void updateStockData(String stockId, SoldStockDto soldStockDto);

  List<Company> saveAll(List<Company> companies);

  StockStatistics calculateStats(String id);

  List<Company> getMyInvestmentStocks();
}
