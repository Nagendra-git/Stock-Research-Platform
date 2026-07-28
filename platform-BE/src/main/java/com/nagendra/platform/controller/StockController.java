package com.nagendra.platform.controller;

import com.nagendra.platform.dto.AddStockRequestDto;
import com.nagendra.platform.dto.SoldStockDto;
import com.nagendra.platform.dto.StockStatistics;
import com.nagendra.platform.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockController {

  private final StockService stockService;

  @PostMapping
  public ResponseEntity<String> upsertStocks(@RequestBody AddStockRequestDto requestDto) {
    stockService.addStocks(requestDto);
    return new ResponseEntity<>("Successfully added stocks", HttpStatus.OK);
  }

  @DeleteMapping
  public ResponseEntity<String> deleteStock(@RequestParam final String id) {
    stockService.deleteStock(id);
    return new ResponseEntity<>("Successfully deleted", HttpStatus.OK);
  }

  @PutMapping("/{stockId}")
  public ResponseEntity<String> updateStockData(
      @PathVariable final String stockId, @RequestBody SoldStockDto soldStockDto) {
    stockService.updateStockData(stockId, soldStockDto);
    return new ResponseEntity<>("Stock updated successfully", HttpStatus.OK);
  }

  @GetMapping("/statistics")
  public ResponseEntity<StockStatistics> statistics(@RequestParam final String id) {
    return new ResponseEntity<>(stockService.calculateStats(id), HttpStatus.OK);
  }
}
