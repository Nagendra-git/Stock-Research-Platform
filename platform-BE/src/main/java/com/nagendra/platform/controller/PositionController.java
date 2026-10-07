package com.nagendra.platform.controller;

import com.nagendra.platform.dto.marketdata.CreatePositionRequest;
import com.nagendra.platform.dto.marketdata.PositionSummaryResponse;
import com.nagendra.platform.dto.position.PositionResponse;
import com.nagendra.platform.dto.position.PositionResult;
import com.nagendra.platform.enums.ExitReason;
import com.nagendra.platform.service.PositionService;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/positions")
public class PositionController {

  private final PositionService positionService;

  public PositionController(PositionService positionService) {
    this.positionService = positionService;
  }

  /**
   * Opens a new automated position: sizes quantity from capital/risk config, places the entry order
   * (paper or live per trading.mode), and starts watching it against stop-loss/target on every
   * incoming tick.
   */
  @PostMapping
  public ResponseEntity<?> create(@RequestBody CreatePositionRequest request) {
    PositionResult result = positionService.createPosition(request);
    if (!result.isOk()) {
      return ResponseEntity.badRequest().body(Map.of("error", result.getMessage()));
    }
    return ResponseEntity.ok(PositionResponse.from(result.getPosition()));
  }

  @GetMapping
  public ResponseEntity<List<PositionResponse>> all() {
    return ResponseEntity.ok(
        positionService.listAll().stream().map(PositionResponse::from).toList());
  }

  @GetMapping("/open")
  public ResponseEntity<List<PositionResponse>> open() {
    return ResponseEntity.ok(
        positionService.listOpen().stream().map(PositionResponse::from).toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<PositionResponse> one(@PathVariable String id) {
    return positionService
        .getById(id)
        .map(PositionResponse::from)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  /** Manually exits one position at the current market price regardless of stop-loss/target. */
  @PostMapping("/{id}/exit")
  public ResponseEntity<?> exit(@PathVariable String id) {
    return positionService
        .manualExit(id)
        .map(PositionResponse::from)
        .map(ResponseEntity::ok)
        .orElseGet(
            () ->
                ResponseEntity.badRequest()
                    .body(
                        (PositionResponse)
                            Map.of(
                                "error", "Position was not OPEN or no live price was available")));
  }

  /** Force-exits every OPEN position right now (emergency stop / manual EOD square-off). */
  @PostMapping("/square-off")
  public ResponseEntity<Void> squareOffAll() {
    positionService.squareOffAll(ExitReason.MANUAL);
    return ResponseEntity.accepted().build();
  }

  @GetMapping("/summary")
  public ResponseEntity<PositionSummaryResponse> summary() {
    return ResponseEntity.ok(positionService.getSummary());
  }
}
