package com.nagendra.platform.dto.marketdata;

import com.nagendra.platform.enums.OrderType;
import java.math.BigDecimal;

/**
 * What the caller supplies to open one automated position. Only instrumentKey is mandatory -
 * everything else falls back to trading.* defaults in application.yml (see TradingProperties).
 *
 * <p>Sizing precedence: explicit quantity > capitalAllocated > trading.maxCapitalPerTradePercent.
 * Stop-loss precedence: stopLossPrice > stopLossPercent > trading.defaultStopLossPercent. Target
 * precedence: targetPrice > targetPercent > trading.defaultTargetPercent.
 */
public class CreatePositionRequest {

  private String instrumentKey;

  private String symbol;

  private OrderType orderType = OrderType.MARKET;

  /** Required if orderType == LIMIT. */
  private BigDecimal limitPrice;

  /**
   * How much capital (rupees) to put into this one stock. Omit to use
   * trading.maxCapitalPerTradePercent of totalCapital.
   */
  private BigDecimal capitalAllocated;

  /** Explicit share count. If set, this wins over capitalAllocated/risk-based sizing entirely. */
  private Integer quantity;

  private BigDecimal stopLossPrice;
  private BigDecimal stopLossPercent;

  private BigDecimal targetPrice;
  private BigDecimal targetPercent;

  /** If set (>0), a trailing stop replaces the fixed stop-loss once price moves in your favor. */
  private BigDecimal trailingStopPercent;

  /** "D" delivery/CNC or "I" intraday/MIS. Defaults to trading.defaultProduct. */
  private String product;

  public String getInstrumentKey() {
    return instrumentKey;
  }

  public void setInstrumentKey(String instrumentKey) {
    this.instrumentKey = instrumentKey;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public OrderType getOrderType() {
    return orderType;
  }

  public void setOrderType(OrderType orderType) {
    this.orderType = orderType;
  }

  public BigDecimal getLimitPrice() {
    return limitPrice;
  }

  public void setLimitPrice(BigDecimal limitPrice) {
    this.limitPrice = limitPrice;
  }

  public BigDecimal getCapitalAllocated() {
    return capitalAllocated;
  }

  public void setCapitalAllocated(BigDecimal capitalAllocated) {
    this.capitalAllocated = capitalAllocated;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getStopLossPrice() {
    return stopLossPrice;
  }

  public void setStopLossPrice(BigDecimal stopLossPrice) {
    this.stopLossPrice = stopLossPrice;
  }

  public BigDecimal getStopLossPercent() {
    return stopLossPercent;
  }

  public void setStopLossPercent(BigDecimal stopLossPercent) {
    this.stopLossPercent = stopLossPercent;
  }

  public BigDecimal getTargetPrice() {
    return targetPrice;
  }

  public void setTargetPrice(BigDecimal targetPrice) {
    this.targetPrice = targetPrice;
  }

  public BigDecimal getTargetPercent() {
    return targetPercent;
  }

  public void setTargetPercent(BigDecimal targetPercent) {
    this.targetPercent = targetPercent;
  }

  public BigDecimal getTrailingStopPercent() {
    return trailingStopPercent;
  }

  public void setTrailingStopPercent(BigDecimal trailingStopPercent) {
    this.trailingStopPercent = trailingStopPercent;
  }

  public String getProduct() {
    return product;
  }

  public void setProduct(String product) {
    this.product = product;
  }
}
