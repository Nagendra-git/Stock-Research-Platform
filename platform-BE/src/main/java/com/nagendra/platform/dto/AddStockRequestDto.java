package com.nagendra.platform.dto;

import java.util.List;
import lombok.Data;

@Data
public class AddStockRequestDto {
  List<StockRequestDto> stocks;
  private String stockCategory;
}
