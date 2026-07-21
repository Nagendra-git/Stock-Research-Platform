package com.nagendra.platform.models;

import com.nagendra.platform.enums.StockCategory;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("stock_category_mapping")
@EqualsAndHashCode(callSuper = false)
public class StockCategoryMapping extends Audit {

  @Id private String id;

  private String stockId;

  private StockCategory category;
}
