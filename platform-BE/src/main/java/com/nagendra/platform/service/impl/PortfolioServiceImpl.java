package com.nagendra.platform.service.impl;

import com.nagendra.platform.dto.portfolio.*;
import com.nagendra.platform.models.Stock;
import com.nagendra.platform.service.PortfolioService;
import com.nagendra.platform.service.StockService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService {

  private final StockService stockService;

  @Override
  public PortfolioResponseDto getPortfolioSummary() {

    List<Stock> stocks = stockService.getMyInvestmentStocks();

    BigDecimal totalInvestment = BigDecimal.ZERO;
    BigDecimal totalCurrentValue = BigDecimal.ZERO;

    BigDecimal bookedInvestment = BigDecimal.ZERO;
    BigDecimal bookedValue = BigDecimal.ZERO;

    long gainStocks = 0;
    long lossStocks = 0;
    long holdingStocks = 0;
    long soldStocks = 0;

    List<StockGainLossDto> stockDtos = new ArrayList<>();

    for (Stock stock : stocks) {

      BigDecimal investment =
          stock.getBoughtPrice().multiply(BigDecimal.valueOf(stock.getQuantity()));

      totalInvestment = totalInvestment.add(investment);

      if (stock.getSoldPrice() != null) {

        soldStocks++;

        BigDecimal soldValue =
            stock.getSoldPrice().multiply(BigDecimal.valueOf(stock.getQuantity()));

        bookedInvestment = bookedInvestment.add(investment);
        bookedValue = bookedValue.add(soldValue);

        BigDecimal gainLoss = soldValue.subtract(investment);

        if (gainLoss.signum() >= 0) gainStocks++;
        else lossStocks++;

        stockDtos.add(createStockDto(stock.getSymbol(), investment, soldValue));

      } else {

        holdingStocks++;

        BigDecimal currentValue =
            stock.getStockPrice().multiply(BigDecimal.valueOf(stock.getQuantity()));

        totalCurrentValue = totalCurrentValue.add(currentValue);

        BigDecimal gainLoss = currentValue.subtract(investment);

        if (gainLoss.signum() >= 0) gainStocks++;
        else lossStocks++;

        stockDtos.add(createStockDto(stock.getSymbol(), investment, currentValue));
      }
    }

    BigDecimal holdingGain = totalCurrentValue.subtract(totalInvestment.subtract(bookedInvestment));
    BigDecimal bookedGain = bookedValue.subtract(bookedInvestment);
    BigDecimal overallGain = holdingGain.add(bookedGain);

    return PortfolioResponseDto.builder()
        .portfolioSummary(
            PortfolioSummaryDto.builder()
                .investment(totalInvestment)
                .currentValue(totalCurrentValue)
                .overall(createGainLoss(totalInvestment, overallGain))
                .booked(createGainLoss(bookedInvestment, bookedGain))
                .holding(createGainLoss(totalInvestment.subtract(bookedInvestment), holdingGain))
                .build())
        .statistics(
            PortfolioStatisticsDto.builder()
                .totalStocks(stocks.size())
                .holdingStocks(holdingStocks)
                .soldStocks(soldStocks)
                .gainStocks(gainStocks)
                .lossStocks(lossStocks)
                .build())
        .stocks(stockDtos)
        .build();
  }

  private StockGainLossDto createStockDto(
      String symbol, BigDecimal investment, BigDecimal currentValue) {

    BigDecimal gain = currentValue.subtract(investment);

    return StockGainLossDto.builder()
        .symbol(symbol)
        .gainLoss(createGainLoss(investment, gain))
        .build();
  }

  private GainLossDto createGainLoss(BigDecimal investment, BigDecimal gain) {

    BigDecimal percentage = BigDecimal.ZERO;

    if (investment.compareTo(BigDecimal.ZERO) > 0) {
      percentage =
          gain.multiply(BigDecimal.valueOf(100)).divide(investment, 2, RoundingMode.HALF_UP);
    }

    return GainLossDto.builder()
        .type(gain.signum() >= 0 ? "GAIN" : "LOSS")
        .amount(gain.abs())
        .percentage(percentage.abs())
        .build();
  }
}
