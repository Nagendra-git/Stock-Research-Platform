package com.nagendra.platform.models;

import com.nagendra.platform.enums.ExitReason;
import com.nagendra.platform.enums.PositionStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * One automated long-equity trade: how much capital was put in, at what price, and the
 * stop-loss/target that PositionMonitorService watches for on every incoming tick.
 */
@Data
@Document(collection = "positions")
@EqualsAndHashCode(callSuper = false)
public class Position extends Audit {
  @Id private String id;

  @Indexed private String instrumentKey;
  private String symbol;

  private PositionStatus status;

  // Sizing
  private BigDecimal capitalAllocated;
  private int quantity;

  // Entry
  private BigDecimal requestedEntryPrice; // null for MARKET entries
  private BigDecimal entryPrice; // actual/estimated fill price
  private String entryOrderId;
  private LocalDateTime entryTime;

  // Risk management - re-evaluated on every tick by PositionMonitorService
  private BigDecimal stopLossPrice;
  private BigDecimal targetPrice;
  private boolean trailingStopEnabled;
  private BigDecimal trailingStopPercent;
  private BigDecimal highestPriceSinceEntry;

  // Exit
  private BigDecimal exitPrice;
  private String exitOrderId;
  private LocalDateTime exitTime;
  private ExitReason exitReason;
  private BigDecimal realizedPnl;

  private String product; // "D" or "I"
}
