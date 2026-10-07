package com.nagendra.platform.trading;


import com.nagendra.platform.config.TradingProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/**
 * Turns "I have this much capital and this stop-loss" into "buy this
 * many shares" using two independent caps, taking whichever is smaller:
 *
 *   1. Capital cap:  capitalAllocated / entryPrice
 *   2. Risk cap:     (totalCapital * riskPerTradePercent%) / (entryPrice - stopLossPrice)
 *
 * The risk cap means even if you allocate a lot of capital to a stock,
 * you never buy so many shares that hitting the stop-loss loses more
 * than riskPerTradePercent% of your total capital.
 */
@Service
public class PositionSizingService {

    private final TradingProperties properties;

    public PositionSizingService(TradingProperties properties) {
        this.properties = properties;
    }

    /**
     * @param capitalAllocated rupees earmarked for this one stock (already capped by
     *                         trading.maxCapitalPerTradePercent by the caller)
     * @param entryPrice       expected/actual entry price per share
     * @param stopLossPrice    the stop-loss price for this position (must be below entryPrice for longs)
     * @return computed quantity, or empty if capital/risk budget doesn't allow even 1 share
     */
    public SizingResult calculateQuantity(BigDecimal capitalAllocated, BigDecimal entryPrice, BigDecimal stopLossPrice) {
        if (entryPrice == null || entryPrice.signum() <= 0) {
            return SizingResult.rejected("Entry price must be positive");
        }
        if (capitalAllocated == null || capitalAllocated.signum() <= 0) {
            return SizingResult.rejected("Capital allocated must be positive");
        }

        int qtyByCapital = capitalAllocated.divide(entryPrice, 0, RoundingMode.DOWN).intValue();

        int qtyByRisk = Integer.MAX_VALUE;
        if (stopLossPrice != null && stopLossPrice.signum() > 0) {
            BigDecimal riskPerShare = entryPrice.subtract(stopLossPrice);
            if (riskPerShare.signum() > 0) {
                BigDecimal maxRiskAmount = properties.getTotalCapital()
                        .multiply(properties.getRiskPerTradePercent())
                        .divide(BigDecimal.valueOf(100), 4, RoundingMode.DOWN);
                qtyByRisk = maxRiskAmount.divide(riskPerShare, 0, RoundingMode.DOWN).intValue();
            }
        }

        int quantity = Math.min(qtyByCapital, qtyByRisk);
        if (quantity < 1) {
            return SizingResult.rejected(String.format(
                    "Computed quantity is 0 (capital-based cap=%d, risk-based cap=%d) - "
                            + "increase capitalAllocated, widen the stop-loss, or raise trading.riskPerTradePercent",
                    qtyByCapital, qtyByRisk == Integer.MAX_VALUE ? -1 : qtyByRisk));
        }
        return SizingResult.accepted(quantity);
    }

    public static class SizingResult {
        private final boolean ok;
        private final int quantity;
        private final String message;

        private SizingResult(boolean ok, int quantity, String message) {
            this.ok = ok;
            this.quantity = quantity;
            this.message = message;
        }

        static SizingResult accepted(int quantity) { return new SizingResult(true, quantity, null); }
        static SizingResult rejected(String message) { return new SizingResult(false, 0, message); }

        public boolean isOk() { return ok; }
        public int getQuantity() { return quantity; }
        public String getMessage() { return message; }
    }
}
