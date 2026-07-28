package com.nagendra.platform.models;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("momentum_score")
@EqualsAndHashCode(callSuper = false)
public class MomentumScore extends Audit {

  @Id private String id;

  private String isin;
  private Integer fundamentalScore;
  private Double priceScore;
  private Double volumeScore;
  private Double trendScore;
  private Double momentumScore;
  private Double volatilityScore;
}
