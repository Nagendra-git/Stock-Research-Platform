package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Depth {

  @JsonProperty("buy")
  private List<DepthLevel> buy;

  @JsonProperty("sell")
  private List<DepthLevel> sell;
}
