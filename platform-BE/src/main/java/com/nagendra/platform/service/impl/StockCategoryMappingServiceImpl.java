package com.nagendra.platform.service.impl;

import com.nagendra.platform.models.StockCategoryMapping;
import com.nagendra.platform.repository.StockCategoryMappingRepository;
import com.nagendra.platform.service.StockCategoryMappingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockCategoryMappingServiceImpl implements StockCategoryMappingService {

  private final StockCategoryMappingRepository mappingRepository;

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void saveAll(List<StockCategoryMapping> categoryMappings) {
    mappingRepository.saveAll(categoryMappings);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRED)
  public void removeStockCategory(String id) {
    StockCategoryMapping categoryMapping = getByStockId(id);
    mappingRepository.deleteById(categoryMapping.getId());
  }

  private StockCategoryMapping getByStockId(String id) {
    return mappingRepository.findByStockId(id).orElseThrow();
  }
}
