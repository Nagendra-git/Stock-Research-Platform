package com.nagendra.platform.trading;


import com.nagendra.platform.dto.order.OrderRequest;
import com.nagendra.platform.dto.order.OrderResult;
import com.nagendra.platform.enums.OrderType;
import com.nagendra.platform.service.OrderExecutionService;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default, safe implementation: simulates an instant fill and never
 * calls Upstox. MARKET orders "fill" at referencePrice (the last known
 * LTP); LIMIT orders "fill" at their own limit price. Use this until
 * you've verified the strategy end-to-end, then switch trading.mode
 * to LIVE.
 */
public class PaperOrderExecutionService implements OrderExecutionService {

    private static final Logger log = LoggerFactory.getLogger(PaperOrderExecutionService.class);
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public OrderResult placeOrder(OrderRequest request) {
        BigDecimal fillPrice = request.getOrderType() == OrderType.LIMIT
                ? request.getPrice()
                : request.getReferencePrice();

        if (fillPrice == null) {
            return OrderResult.rejected("No reference/limit price available to simulate a fill");
        }

        String paperOrderId = "PAPER-" + sequence.getAndIncrement();
        log.info("[PAPER] {} {} x {} @ ~{} ({}, product={}) -> {}",
                request.getTransactionType(), request.getQuantity(), request.getInstrumentKey(),
                fillPrice, request.getOrderType(), request.getProduct(), paperOrderId);
        return OrderResult.accepted(paperOrderId, fillPrice);
    }
}
