package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FullStatement {

  @JsonProperty("particular")
  private String particular;

  @JsonProperty("history")
  private List<StatementHistory> history;
}
