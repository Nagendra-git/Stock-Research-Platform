package com.nagendra.platform.service;

import com.nagendra.platform.dto.StackStatisticsDto;
import com.nagendra.platform.dto.StockStatistics;

public interface StockStatisticsService {
  StockStatistics calculateStats(StackStatisticsDto stackStatisticsDto);
}
