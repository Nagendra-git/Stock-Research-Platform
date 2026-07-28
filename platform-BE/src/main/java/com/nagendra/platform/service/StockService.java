package com.nagendra.platform.service;

import com.nagendra.platform.dto.AddStockRequestDto;
import com.nagendra.platform.dto.SoldStockDto;
import com.nagendra.platform.dto.StockStatistics;
import com.nagendra.platform.models.Stock;
import java.util.List;

public interface StockService {

  void addStocks(AddStockRequestDto requestDto);

  void deleteStock(String id);

  void updateStockData(String stockId, SoldStockDto soldStockDto);

  List<Stock> getMyInvestmentStocks();

  StockStatistics calculateStats(String id);
}
