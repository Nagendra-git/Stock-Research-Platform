package com.nagendra.platform.dto.position;

import com.nagendra.platform.enums.ExitReason;
import com.nagendra.platform.enums.PositionStatus;
import com.nagendra.platform.models.Position;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PositionResponse {

    private String id;
    private String instrumentKey;
    private String symbol;
    private PositionStatus status;
    private int quantity;
    private BigDecimal capitalAllocated;
    private BigDecimal entryPrice;
    private LocalDateTime entryTime;
    private BigDecimal stopLossPrice;
    private BigDecimal targetPrice;
    private boolean trailingStopEnabled;
    private BigDecimal exitPrice;
    private LocalDateTime exitTime;
    private ExitReason exitReason;
    private BigDecimal realizedPnl;
    private String product;

    public static PositionResponse from(Position p) {
        PositionResponse r = new PositionResponse();
        r.id = p.getId();
        r.instrumentKey = p.getInstrumentKey();
        r.symbol = p.getSymbol();
        r.status = p.getStatus();
        r.quantity = p.getQuantity();
        r.capitalAllocated = p.getCapitalAllocated();
        r.entryPrice = p.getEntryPrice();
        r.entryTime = p.getEntryTime();
        r.stopLossPrice = p.getStopLossPrice();
        r.targetPrice = p.getTargetPrice();
        r.trailingStopEnabled = p.isTrailingStopEnabled();
        r.exitPrice = p.getExitPrice();
        r.exitTime = p.getExitTime();
        r.exitReason = p.getExitReason();
        r.realizedPnl = p.getRealizedPnl();
        r.product = p.getProduct();
        return r;
    }
}
