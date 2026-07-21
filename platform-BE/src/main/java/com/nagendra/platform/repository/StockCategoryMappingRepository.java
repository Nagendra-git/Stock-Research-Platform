package com.nagendra.platform.repository;

import com.nagendra.platform.models.StockCategoryMapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockCategoryMappingRepository extends MongoRepository<StockCategoryMapping, String> {

}
