package com.nagendra.platform.service;

import com.nagendra.platform.dto.AddStockRequestDto;
import com.nagendra.platform.dto.client.MarketQuoteResponse;
import org.springframework.stereotype.Service;

@Service
public interface StockService {
  void addStocks(AddStockRequestDto requestDto);

  void deleteStock(String id);

  MarketQuoteResponse getStockInfo();
}
