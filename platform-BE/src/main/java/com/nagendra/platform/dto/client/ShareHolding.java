package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class ShareHolding {

  @JsonProperty("category")
  private String category;

  @JsonProperty("history")
  private List<ShareHoldingHistory> history;
}
