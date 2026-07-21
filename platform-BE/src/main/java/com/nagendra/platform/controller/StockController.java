package com.nagendra.platform.controller;

import com.nagendra.platform.dto.AddStockRequestDto;
import com.nagendra.platform.dto.BoughtStockDto;
import com.nagendra.platform.dto.client.MarketQuoteResponse;
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

  @GetMapping
  public ResponseEntity<MarketQuoteResponse> getStocksInfo() {
    return new ResponseEntity<>(stockService.getStockInfo(), HttpStatus.OK);
  }

  @PutMapping("/{stockId}")
  public ResponseEntity<String> updateStockData(
      @PathVariable final String stockId, @RequestBody BoughtStockDto boughtStockDto) {
    stockService.updateStockData(stockId, boughtStockDto);
    return new ResponseEntity<>("Stock updated successfully", HttpStatus.OK);
  }
}
