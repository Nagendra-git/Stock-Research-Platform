package com.nagendra.platform.enums;

public enum PositionStatus {
  /** Entry order placed but not yet confirmed filled. */
  PENDING_ENTRY,
  /** Entry filled, position live, being watched by PositionMonitorService. */
  OPEN,
  /** Exit filled - terminal state. See exitReason for why. */
  EXITED,
  /** Entry order was rejected/failed - terminal state, never became live. */
  ENTRY_FAILED
}
