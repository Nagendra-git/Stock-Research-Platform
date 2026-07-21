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

  private String companyName;

  private String symbol;

  private String exchange;

  private String segment;

  private String isin;

  private String instrumentKey;

  private Double stockPrice;

  private Double averagePrice;
}
