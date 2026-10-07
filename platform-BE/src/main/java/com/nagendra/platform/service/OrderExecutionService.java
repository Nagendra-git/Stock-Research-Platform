package com.nagendra.platform.service;

import com.nagendra.platform.dto.order.OrderRequest;
import com.nagendra.platform.dto.order.OrderResult;

/**
 * Places one order and returns whether it was accepted. Exactly one implementation is active at a
 * time, chosen by {@code trading.mode}: PAPER -> PaperOrderExecutionService (no real money, instant
 * simulated fill) LIVE -> UpstoxOrderExecutionService (real order to Upstox, real money)
 */
public interface OrderExecutionService {
  OrderResult placeOrder(OrderRequest request);
}
