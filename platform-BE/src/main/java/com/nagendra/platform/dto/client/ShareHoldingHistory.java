package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ShareHoldingHistory {

  @JsonProperty("value")
  private Double value;

  @JsonProperty("period")
  private String period;
}
