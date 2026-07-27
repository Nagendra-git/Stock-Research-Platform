package com.nagendra.platform.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("company")
@EqualsAndHashCode(callSuper = false)
public class Company extends Audit {

  @Id private String id;

  private String isin;

  private Double stockPrice;

  private Double boughtPrice;

  private Long quantity;

  private Boolean isMomentumScore;

  private Boolean isFundamentalScore;

  private Double expectedQuarterlyResults;
}
