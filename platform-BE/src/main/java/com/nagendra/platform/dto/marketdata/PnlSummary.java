package com.nagendra.platform.dto.marketdata;

import com.nagendra.platform.enums.PnlType;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PnlSummary {

  private BigDecimal amount;

  private BigDecimal percentage;

  private PnlType type;
}
