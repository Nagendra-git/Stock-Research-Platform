package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FinancialStatementData {

  @JsonProperty("type")
  private String type;

  @JsonProperty("time_period")
  private String timePeriod;

  @JsonProperty("units_in")
  private String unitsIn;

  @JsonProperty("income_statement")
  private List<IncomeStatement> incomeStatement;

  @JsonProperty("full_statement")
  private List<FullStatement> fullStatement;
}
