package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KeyRatio {

  @JsonProperty("name")
  private String name;

  @JsonProperty("company_value")
  private String companyValue;

  @JsonProperty("sector_value")
  private String sectorValue;
}
