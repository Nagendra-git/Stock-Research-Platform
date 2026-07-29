package com.nagendra.platform.service;

import com.nagendra.platform.models.WeeklyStocks;
import java.util.List;

public interface WeeklyStockService {

  void addAllStocks(List<WeeklyStocks> weeklyStocks);

  List<WeeklyStocks> getAllStocks();

  void removeAllStocks();
}
