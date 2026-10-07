package com.nagendra.platform.config;

import java.math.BigDecimal;
import java.time.LocalTime;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Risk and execution settings for the automated position-management
 * feature (entry sizing, stop-loss/target, paper vs live orders).
 * Bound from the {@code trading.*} keys in application.yml.
 */
@ConfigurationProperties(prefix = "trading")
public class TradingProperties {

    /** PAPER = simulated fills only, no real order ever leaves this app. LIVE = real Upstox orders. */
    private String mode = "PAPER";

    /** Total capital (in rupees) this bot is allowed to deploy across all open positions. */
    private BigDecimal totalCapital = BigDecimal.ZERO;

    /** Max % of totalCapital that may be allocated to a single stock. */
    private BigDecimal maxCapitalPerTradePercent = BigDecimal.valueOf(10);

    /** Max % of totalCapital allowed to be lost on a single trade if its stop-loss is hit. Drives quantity sizing. */
    private BigDecimal riskPerTradePercent = BigDecimal.valueOf(1);

    /** Hard cap on number of simultaneously open positions. */
    private int maxOpenPositions = 5;

    /** Used when a CreatePositionRequest doesn't specify its own stop-loss. */
    private BigDecimal defaultStopLossPercent = BigDecimal.valueOf(2);

    /** Used when a CreatePositionRequest doesn't specify its own target. */
    private BigDecimal defaultTargetPercent = BigDecimal.valueOf(4);

    /** Upstox product code: "D" = delivery/CNC, "I" = intraday/MIS. */
    private String defaultProduct = "D";

    /** If true, every OPEN position is force-exited at squareOffTime (safety net for intraday). */
    private boolean squareOffEnabled = false;

    private LocalTime squareOffTime = LocalTime.of(15, 15);

    /** Base URL for the live Upstox order-placement API. Note the api-hft host, different from the feed/auth host. */
    private String orderPlaceUrl = "https://api-hft.upstox.com/v3/order/place";

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public boolean isLive() { return "LIVE".equalsIgnoreCase(mode); }

    public BigDecimal getTotalCapital() { return totalCapital; }
    public void setTotalCapital(BigDecimal totalCapital) { this.totalCapital = totalCapital; }

    public BigDecimal getMaxCapitalPerTradePercent() { return maxCapitalPerTradePercent; }
    public void setMaxCapitalPerTradePercent(BigDecimal v) { this.maxCapitalPerTradePercent = v; }

    public BigDecimal getRiskPerTradePercent() { return riskPerTradePercent; }
    public void setRiskPerTradePercent(BigDecimal v) { this.riskPerTradePercent = v; }

    public int getMaxOpenPositions() { return maxOpenPositions; }
    public void setMaxOpenPositions(int maxOpenPositions) { this.maxOpenPositions = maxOpenPositions; }

    public BigDecimal getDefaultStopLossPercent() { return defaultStopLossPercent; }
    public void setDefaultStopLossPercent(BigDecimal v) { this.defaultStopLossPercent = v; }

    public BigDecimal getDefaultTargetPercent() { return defaultTargetPercent; }
    public void setDefaultTargetPercent(BigDecimal v) { this.defaultTargetPercent = v; }

    public String getDefaultProduct() { return defaultProduct; }
    public void setDefaultProduct(String defaultProduct) { this.defaultProduct = defaultProduct; }

    public boolean isSquareOffEnabled() { return squareOffEnabled; }
    public void setSquareOffEnabled(boolean squareOffEnabled) { this.squareOffEnabled = squareOffEnabled; }

    public LocalTime getSquareOffTime() { return squareOffTime; }
    public void setSquareOffTime(LocalTime squareOffTime) { this.squareOffTime = squareOffTime; }

    public String getOrderPlaceUrl() { return orderPlaceUrl; }
    public void setOrderPlaceUrl(String orderPlaceUrl) { this.orderPlaceUrl = orderPlaceUrl; }
}