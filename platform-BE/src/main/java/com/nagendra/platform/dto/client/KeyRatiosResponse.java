package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class KeyRatiosResponse {

  @JsonProperty("status")
  private String status;

  @JsonProperty("data")
  private List<KeyRatio> data;
}
