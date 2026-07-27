package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatementHistory {

  @JsonProperty("value")
  private Double value;

  @JsonProperty("period")
  private String period;

  @JsonProperty("change")
  private String change;
}
