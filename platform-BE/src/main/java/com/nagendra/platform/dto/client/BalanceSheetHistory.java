package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BalanceSheetHistory {

  @JsonProperty("total_asset")
  private Double totalAsset;

  @JsonProperty("total_liability")
  private Double totalLiability;

  @JsonProperty("period")
  private String period;
}
