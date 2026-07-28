package com.nagendra.platform.dto.portfolio;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GainLossDto {

  private String type;
  private BigDecimal amount;
  private BigDecimal percentage;
}
