package com.nagendra.platform.models;

import java.math.BigDecimal;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("stocks")
@EqualsAndHashCode(callSuper = false)
public class Stock extends Audit {

  @Id private String id;

  private String isin;

  private BigDecimal stockPrice;

  private BigDecimal boughtPrice;

  private BigDecimal soldPrice;

  private String symbol;

  private Long quantity;

  private Boolean isMomentumScore;

  private Boolean isFundamentalScore;

  private Double expectedQuarterlyResults;
}
