package com.nagendra.platform.controller;

import com.nagendra.platform.dto.Weekly.WeeklyMomentumPageResponse;
import com.nagendra.platform.dto.filters.TrendAnalysisResponse;
import com.nagendra.platform.dto.filters.WeeklyBuyerSellerAnalysis;
import com.nagendra.platform.service.WeeklyMomentumService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/weekly-momentum")
public class WeeklyMomentumController {

  private final WeeklyMomentumService weeklyMomentumService;

  @GetMapping
  public ResponseEntity<TrendAnalysisResponse> getWeeklyMomentum(
      @RequestParam String isin,
      @RequestParam(required = false, defaultValue = "1") Integer duration) {
    TrendAnalysisResponse momentum = weeklyMomentumService.getWeeklyMomentum(isin, duration);
    return ResponseEntity.ok(momentum);
  }

  @PostMapping
  public ResponseEntity<WeeklyBuyerSellerAnalysis> addWeeklyMomentum(@RequestParam String isin) {
    WeeklyBuyerSellerAnalysis analysis = weeklyMomentumService.addWeeklyAnalysis(isin);
    return ResponseEntity.ok(analysis);
  }

  @GetMapping("/all")
  public ResponseEntity<WeeklyMomentumPageResponse> getWeeklyMomentumForAll(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "swingScore") String sortBy,
      @RequestParam(defaultValue = "desc") String direction) {

    WeeklyMomentumPageResponse momentumList =
        weeklyMomentumService.getWeeklyMomentumForAll(page, size, sortBy, direction);

    return ResponseEntity.ok(momentumList);
  }
}
