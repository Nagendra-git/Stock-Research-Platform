package com.nagendra.platform.dto.order;

import java.math.BigDecimal;

public class OrderResult {

  private final boolean accepted;
  private final String orderId;
  private final BigDecimal fillPrice; // best-known fill/reference price at placement time
  private final String message;

  private OrderResult(boolean accepted, String orderId, BigDecimal fillPrice, String message) {
    this.accepted = accepted;
    this.orderId = orderId;
    this.fillPrice = fillPrice;
    this.message = message;
  }

  public static OrderResult accepted(String orderId, BigDecimal fillPrice) {
    return new OrderResult(true, orderId, fillPrice, "OK");
  }

  public static OrderResult rejected(String message) {
    return new OrderResult(false, null, null, message);
  }

  public boolean isAccepted() {
    return accepted;
  }

  public String getOrderId() {
    return orderId;
  }

  public BigDecimal getFillPrice() {
    return fillPrice;
  }

  public String getMessage() {
    return message;
  }
}
