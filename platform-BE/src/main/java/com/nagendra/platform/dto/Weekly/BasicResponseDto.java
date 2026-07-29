package com.nagendra.platform.dto.Weekly;

import lombok.Data;

@Data
public class BasicResponseDto {
  private String isin;
  private Double weeklyScore;
  private Double monthlyScore;
}
