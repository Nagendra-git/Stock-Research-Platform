package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class ShareHoldingResponse {

  @JsonProperty("status")
  private String status;

  @JsonProperty("data")
  private List<ShareHolding> data;
}
