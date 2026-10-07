package com.nagendra.platform.dto.order;

import com.nagendra.platform.enums.OrderType;
import com.nagendra.platform.enums.TransactionType;
import java.math.BigDecimal;

/**
 * Everything an OrderExecutionService implementation needs to place one order. {@code
 * referencePrice} is only used by the paper-trading implementation to simulate a fill for MARKET
 * orders (the live Upstox client ignores it - the exchange decides the real fill price).
 */
public class OrderRequest {

  private final String instrumentKey;
  private final int quantity;
  private final TransactionType transactionType;
  private final OrderType orderType;
  private final BigDecimal price; // required for LIMIT, ignored for MARKET
  private final BigDecimal referencePrice; // last known LTP, used for paper fills / sanity checks
  private final String product; // "D" delivery, "I" intraday - see Upstox docs
  private final String tag;

  public OrderRequest(
      String instrumentKey,
      int quantity,
      TransactionType transactionType,
      OrderType orderType,
      BigDecimal price,
      BigDecimal referencePrice,
      String product,
      String tag) {
    this.instrumentKey = instrumentKey;
    this.quantity = quantity;
    this.transactionType = transactionType;
    this.orderType = orderType;
    this.price = price;
    this.referencePrice = referencePrice;
    this.product = product;
    this.tag = tag;
  }

  public String getInstrumentKey() {
    return instrumentKey;
  }

  public int getQuantity() {
    return quantity;
  }

  public TransactionType getTransactionType() {
    return transactionType;
  }

  public OrderType getOrderType() {
    return orderType;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public BigDecimal getReferencePrice() {
    return referencePrice;
  }

  public String getProduct() {
    return product;
  }

  public String getTag() {
    return tag;
  }
}
