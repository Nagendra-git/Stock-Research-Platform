package com.nagendra.platform.dto;

import lombok.Data;

@Data
public class AddStockRequestDto {
  private StockDetailsDto stockDetails;
  private String stockCategory;
}
