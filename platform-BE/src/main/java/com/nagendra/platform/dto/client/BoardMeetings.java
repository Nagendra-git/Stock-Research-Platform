package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BoardMeetings {

  @JsonProperty("tickerId")
  private String tickerId;

  @JsonProperty("companyName")
  private String companyName;

  @JsonProperty("remarks")
  private String remarks;

  @JsonProperty("boardMeetDate")
  private String boardMeetDate;

  @JsonProperty("purpose")
  private String purpose;
}
