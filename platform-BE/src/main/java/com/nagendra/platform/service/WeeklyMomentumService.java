package com.nagendra.platform.service;

import com.nagendra.platform.dto.Weekly.BasicResponseDto;
import com.nagendra.platform.dto.filters.TrendAnalysisResponse;
import com.nagendra.platform.models.Instrument;
import com.nagendra.platform.models.WeeklyStocks;
import java.util.List;

public interface WeeklyMomentumService {

  TrendAnalysisResponse getWeeklyMomentum(String isin, Integer duration);

  void addWeeklyMomentum(List<Instrument> instruments);

  List<BasicResponseDto> getWeeklyMomentumForAll();

  void updateWeeklyTrendInMemory(WeeklyStocks stock);
}
