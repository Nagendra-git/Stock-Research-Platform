package com.nagendra.platform.controller;

import com.nagendra.platform.config.UpstoxMarketDataProperties;
import com.nagendra.platform.dto.marketdata.MarketDataResponse;
import com.nagendra.platform.dto.marketdata.MarketDataStatusResponse;
import com.nagendra.platform.service.MarketDataRedisService;
import com.nagendra.platform.service.UpstoxMarketDataManager;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/market-data")
@RequiredArgsConstructor
public class MarketDataController {

  private final UpstoxMarketDataManager manager;
  private final MarketDataRedisService redisService;
  private final UpstoxMarketDataProperties properties;

  @PostMapping("/start")
  public ResponseEntity<Void> start() {
    manager.start();
    return ResponseEntity.accepted().build();
  }

  @PostMapping("/stop")
  public ResponseEntity<Void> stop() {
    manager.stop();
    return ResponseEntity.accepted().build();
  }

  // Note: instrumentKey contains a literal "|" (e.g. NSE_EQ|INE669E01016).
  // Spring maps this fine as a single path variable as long as the client
  // URL-encodes it if needed; using {*instrumentKey} avoids ambiguity.
  @GetMapping("/{instrumentKey}")
  public ResponseEntity<MarketDataResponse> getOne(@PathVariable String instrumentKey) {
    return redisService
        .getMarketData(instrumentKey)
        .map(MarketDataResponse::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping
  public ResponseEntity<List<MarketDataResponse>> getAll() {
    List<MarketDataResponse> all =
        redisService.getAllMarketData().values().stream().map(MarketDataResponse::from).toList();
    return ResponseEntity.ok(all);
  }

  @GetMapping("/status")
  public ResponseEntity<MarketDataStatusResponse> status() {
    ZoneId zoneId = ZoneId.of(properties.getMarketData().getTimeZone());
    LocalDate today = LocalDate.now(zoneId);
    MarketDataStatusResponse status =
        new MarketDataStatusResponse(
            properties.getMarketData().isEnabled(),
            manager.isConnected(),
            manager.isConnected(),
            today.toString(),
            redisService.getCurrentRedisKey(),
            manager.getSubscribedInstrumentCount());
    return ResponseEntity.ok(status);
  }
}
