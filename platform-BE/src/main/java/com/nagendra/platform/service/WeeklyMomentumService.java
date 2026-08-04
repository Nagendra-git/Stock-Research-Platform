package com.nagendra.platform.service;

import com.nagendra.platform.dto.Weekly.WeeklyMomentumPageResponse;
import com.nagendra.platform.dto.filters.TrendAnalysisResponse;
import com.nagendra.platform.dto.filters.WeeklyBuyerSellerAnalysis;
import com.nagendra.platform.models.Instrument;
import java.util.List;

public interface WeeklyMomentumService {

  TrendAnalysisResponse getWeeklyMomentum(String isin, Integer duration);

  void addWeeklyMomentum(List<Instrument> instruments);

  WeeklyMomentumPageResponse getWeeklyMomentumForAll(
      int page, int size, String sortBy, String direction);

  WeeklyBuyerSellerAnalysis addWeeklyAnalysis(String isin);
}
