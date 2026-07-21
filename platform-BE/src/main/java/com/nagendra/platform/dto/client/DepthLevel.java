package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DepthLevel {

  @JsonProperty("quantity")
  private Long quantity;

  @JsonProperty("price")
  private Double price;

  @JsonProperty("orders")
  private Integer orders;
}
