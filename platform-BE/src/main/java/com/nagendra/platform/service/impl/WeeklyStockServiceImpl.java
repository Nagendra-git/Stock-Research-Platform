package com.nagendra.platform.service.impl;

import com.nagendra.platform.models.WeeklyStocks;
import com.nagendra.platform.repository.WeeklyStockRepository;
import com.nagendra.platform.service.WeeklyStockService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeeklyStockServiceImpl implements WeeklyStockService {

  private final WeeklyStockRepository stockRepository;

  @Override
  public void addAllStocks(List<WeeklyStocks> weeklyStocks) {
    stockRepository.saveAll(weeklyStocks);
  }

  @Override
  public List<WeeklyStocks> getAllStocks() {
    return stockRepository.findAll();
  }

  @Override
  public void removeAllStocks() {
    stockRepository.deleteAll();
  }
}
