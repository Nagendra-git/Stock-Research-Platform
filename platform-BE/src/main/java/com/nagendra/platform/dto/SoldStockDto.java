package com.nagendra.platform.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class SoldStockDto {

  private BigDecimal soldPrice;

  private Long quantity;
}
