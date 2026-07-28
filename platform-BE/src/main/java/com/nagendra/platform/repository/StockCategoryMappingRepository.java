package com.nagendra.platform.repository;

import com.nagendra.platform.enums.StockCategory;
import com.nagendra.platform.models.StockCategoryMapping;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockCategoryMappingRepository
    extends MongoRepository<StockCategoryMapping, String> {

  Optional<StockCategoryMapping> findByStockId(String id);

  List<StockCategoryMapping> findByCategory(StockCategory stockCategory);
}
