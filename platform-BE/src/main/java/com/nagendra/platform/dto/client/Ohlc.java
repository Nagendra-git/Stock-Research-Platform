package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Ohlc {
  @JsonProperty("open")
  private Double open;

  @JsonProperty("high")
  private Double high;

  @JsonProperty("low")
  private Double low;

  @JsonProperty("close")
  private Double close;
}
