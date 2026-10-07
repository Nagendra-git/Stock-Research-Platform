package com.nagendra.platform.dto.marketdata;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Internal representation of the latest tick for one instrument. This is what gets serialized as
 * JSON and stored in the Redis hash.
 *
 * <p>Field coverage follows the ACTUAL documented V3 "full" feed (feeds -> instrumentKey ->
 * fullFeed -> marketFF / indexFF), not the "oc"/"eFeedDetails" shape - see the notes in
 * MarketDataFeedV3.proto. bidAskQuote in the real feed is a list of up to 5 depth levels; we store
 * the best (first) level here for simplicity and keep the BigDecimal contract requested for
 * financial fields.
 */
public class MarketData implements Serializable {

  private String instrumentKey;

  // LTPC
  private BigDecimal ltp;
  private Long lastTradedTime;
  private Long lastTradedQuantity;
  private BigDecimal closePrice;

  // Best bid/ask (first depth level of marketLevel.bidAskQuote)
  private BigDecimal bidPrice;
  private Long bidQuantity;
  private BigDecimal askPrice;
  private Long askQuantity;

  // Option Greeks (NSE_FO only - null otherwise)
  private BigDecimal
      underlyingPrice; // not separately provided by V3 full feed; kept for DTO compatibility, may
                       // be null
  private BigDecimal impliedVolatility;
  private BigDecimal delta;
  private BigDecimal theta;
  private BigDecimal gamma;
  private BigDecimal vega;
  private BigDecimal rho;

  // Extended feed metadata (marketFF top-level fields)
  private BigDecimal averageTradedPrice;
  private Long volumeTradedToday;
  private Long openInterest;
  private Long totalBuyQuantity;
  private Long totalSellQuantity;

  private Long currentTimestamp;
  private LocalDateTime updatedAt;

  public String getInstrumentKey() {
    return instrumentKey;
  }

  public void setInstrumentKey(String instrumentKey) {
    this.instrumentKey = instrumentKey;
  }

  public BigDecimal getLtp() {
    return ltp;
  }

  public void setLtp(BigDecimal ltp) {
    this.ltp = ltp;
  }

  public Long getLastTradedTime() {
    return lastTradedTime;
  }

  public void setLastTradedTime(Long lastTradedTime) {
    this.lastTradedTime = lastTradedTime;
  }

  public Long getLastTradedQuantity() {
    return lastTradedQuantity;
  }

  public void setLastTradedQuantity(Long lastTradedQuantity) {
    this.lastTradedQuantity = lastTradedQuantity;
  }

  public BigDecimal getClosePrice() {
    return closePrice;
  }

  public void setClosePrice(BigDecimal closePrice) {
    this.closePrice = closePrice;
  }

  public BigDecimal getBidPrice() {
    return bidPrice;
  }

  public void setBidPrice(BigDecimal bidPrice) {
    this.bidPrice = bidPrice;
  }

  public Long getBidQuantity() {
    return bidQuantity;
  }

  public void setBidQuantity(Long bidQuantity) {
    this.bidQuantity = bidQuantity;
  }

  public BigDecimal getAskPrice() {
    return askPrice;
  }

  public void setAskPrice(BigDecimal askPrice) {
    this.askPrice = askPrice;
  }

  public Long getAskQuantity() {
    return askQuantity;
  }

  public void setAskQuantity(Long askQuantity) {
    this.askQuantity = askQuantity;
  }

  public BigDecimal getUnderlyingPrice() {
    return underlyingPrice;
  }

  public void setUnderlyingPrice(BigDecimal underlyingPrice) {
    this.underlyingPrice = underlyingPrice;
  }

  public BigDecimal getImpliedVolatility() {
    return impliedVolatility;
  }

  public void setImpliedVolatility(BigDecimal impliedVolatility) {
    this.impliedVolatility = impliedVolatility;
  }

  public BigDecimal getDelta() {
    return delta;
  }

  public void setDelta(BigDecimal delta) {
    this.delta = delta;
  }

  public BigDecimal getTheta() {
    return theta;
  }

  public void setTheta(BigDecimal theta) {
    this.theta = theta;
  }

  public BigDecimal getGamma() {
    return gamma;
  }

  public void setGamma(BigDecimal gamma) {
    this.gamma = gamma;
  }

  public BigDecimal getVega() {
    return vega;
  }

  public void setVega(BigDecimal vega) {
    this.vega = vega;
  }

  public BigDecimal getRho() {
    return rho;
  }

  public void setRho(BigDecimal rho) {
    this.rho = rho;
  }

  public BigDecimal getAverageTradedPrice() {
    return averageTradedPrice;
  }

  public void setAverageTradedPrice(BigDecimal averageTradedPrice) {
    this.averageTradedPrice = averageTradedPrice;
  }

  public Long getVolumeTradedToday() {
    return volumeTradedToday;
  }

  public void setVolumeTradedToday(Long volumeTradedToday) {
    this.volumeTradedToday = volumeTradedToday;
  }

  public Long getOpenInterest() {
    return openInterest;
  }

  public void setOpenInterest(Long openInterest) {
    this.openInterest = openInterest;
  }

  public Long getTotalBuyQuantity() {
    return totalBuyQuantity;
  }

  public void setTotalBuyQuantity(Long totalBuyQuantity) {
    this.totalBuyQuantity = totalBuyQuantity;
  }

  public Long getTotalSellQuantity() {
    return totalSellQuantity;
  }

  public void setTotalSellQuantity(Long totalSellQuantity) {
    this.totalSellQuantity = totalSellQuantity;
  }

  public Long getCurrentTimestamp() {
    return currentTimestamp;
  }

  public void setCurrentTimestamp(Long currentTimestamp) {
    this.currentTimestamp = currentTimestamp;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
