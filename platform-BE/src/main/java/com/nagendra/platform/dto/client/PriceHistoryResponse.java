package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PriceHistoryResponse {

  @JsonProperty("status")
  private String status;

  @JsonProperty("data")
  private PriceHistoryData data;
}
