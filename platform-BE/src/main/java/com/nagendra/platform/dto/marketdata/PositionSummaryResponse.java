package com.nagendra.platform.dto.marketdata;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PositionSummaryResponse {

    private PortfolioSummary portfolioSummary;

    private PositionStatistics statistics;

    private List<PositionGainLoss> stocks;
}