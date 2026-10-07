package com.nagendra.platform.dto.marketdata;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PositionStatistics {

  private long gainStocks;

  private long holdingStocks;

  private long lossStocks;

  private long soldStocks;

  private long totalStocks;
}
