package com.nagendra.platform.dto.filters;

import lombok.Data;

@Data
public class TrendAnalysisResponse {

  private String isin;


  private WeeklyTrendScore weeklyScores;

  private WeeklyTrendScore monthlyScores;
}
