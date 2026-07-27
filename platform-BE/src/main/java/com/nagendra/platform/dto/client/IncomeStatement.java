package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
public class IncomeStatement {

  @JsonProperty("category")
  private String category;

  @JsonProperty("history")
  private List<StatementHistory> history;
}
