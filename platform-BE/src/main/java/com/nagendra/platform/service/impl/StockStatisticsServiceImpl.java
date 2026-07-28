package com.nagendra.platform.service.impl;

import com.nagendra.platform.dto.StackStatisticsDto;
import com.nagendra.platform.dto.StockStatistics;
import com.nagendra.platform.dto.portfolio.GainLossDto;
import com.nagendra.platform.service.StockStatisticsService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class StockStatisticsServiceImpl implements StockStatisticsService {

  @Override
  public StockStatistics calculateStats(StackStatisticsDto dto) {

    StockStatistics stats = new StockStatistics();

    stats.setBoughtPrice(dto.getBoughtPrice());
    stats.setSoldPrice(dto.getSoldPrice());
    stats.setCurrentPrice(dto.getCurrentPrice());
    stats.setQuantity(dto.getQuantity());

    // Calculate current (holding) performance
    if (dto.getCurrentPrice() != null) {
      GainLossDto current =
          calculateGainLoss(dto.getBoughtPrice(), dto.getCurrentPrice(), dto.getQuantity());

      stats.setCurrentPerformance(current);
    }

    // Calculate booked (sold) performance only if soldPrice exists
    if (dto.getSoldPrice() != null) {
      GainLossDto booked =
          calculateGainLoss(dto.getBoughtPrice(), dto.getSoldPrice(), dto.getQuantity());

      stats.setBookedPerformance(booked);
    }

    return stats;
  }

  private GainLossDto calculateGainLoss(BigDecimal buyPrice, BigDecimal price, Long quantity) {

    BigDecimal qty = BigDecimal.valueOf(quantity);

    BigDecimal amount = price.subtract(buyPrice).multiply(qty);

    BigDecimal percentage = BigDecimal.ZERO;

    if (buyPrice.compareTo(BigDecimal.ZERO) > 0) {
      percentage =
          price
              .subtract(buyPrice)
              .multiply(BigDecimal.valueOf(100))
              .divide(buyPrice, 2, RoundingMode.HALF_UP);
    }

    return GainLossDto.builder()
        .type(amount.signum() >= 0 ? "GAIN" : "LOSS")
        .amount(amount.abs())
        .percentage(percentage.abs())
        .build();
  }
}
