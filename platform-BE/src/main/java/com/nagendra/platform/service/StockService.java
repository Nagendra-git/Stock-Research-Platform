package com.nagendra.platform.service;

import com.nagendra.platform.dto.AddStockRequestDto;
import com.nagendra.platform.dto.BoughtStockDto;
import com.nagendra.platform.dto.client.MarketQuoteResponse;
import com.nagendra.platform.models.Company;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface StockService {
  void addStocks(AddStockRequestDto requestDto);

  void deleteStock(String id);

  MarketQuoteResponse getStockInfo();

  void updateStockData(String stockId, BoughtStockDto boughtStockDto);

  List<Company> saveAll(List<Company> companies);
}
