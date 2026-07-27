package com.nagendra.platform.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CorporateActionResponse {

  @JsonProperty("bonus")
  private List<String> bonus;

  @JsonProperty("dividend")
  private List<String> dividend;

  @JsonProperty("rights")
  private List<String> rights;

  @JsonProperty("splits")
  private List<String> splits;

  @JsonProperty("annualGeneralMeeting")
  private List<String> annualGeneralMeeting;

  @JsonProperty("boardMeetings")
  private List<BoardMeetings> boardMeetings;
}
