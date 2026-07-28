package com.nagendra.platform.service;

import com.nagendra.platform.models.StockCategoryMapping;
import java.util.List;

public interface StockCategoryMappingService {
  void saveAll(List<StockCategoryMapping> categoryMappings);

  void removeStockCategory(String id);

  List<StockCategoryMapping> getStockCategoryMappings();

  void deleteAll(List<StockCategoryMapping> mappingsToRemove);

  void saveMapping(StockCategoryMapping mapping);

  List<String> getMyInvestmentStockIds();
}
