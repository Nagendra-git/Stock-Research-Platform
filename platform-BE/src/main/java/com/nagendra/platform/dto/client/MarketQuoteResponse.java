package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketQuoteResponse {

  @JsonProperty("status")
  private String status;

  @JsonProperty("data")
  private Map<String, Quote> data;
}
