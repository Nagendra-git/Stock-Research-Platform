package com.nagendra.platform.controller;

import com.nagendra.platform.dto.Weekly.BasicResponseDto;
import com.nagendra.platform.dto.filters.TrendAnalysisResponse;
import com.nagendra.platform.service.WeeklyMomentumService;
import java.util.ArrayList;
import java.util.List;
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
  public ResponseEntity<String> addWeeklyMomentum() {
    weeklyMomentumService.addWeeklyMomentum(new ArrayList<>());
    return ResponseEntity.ok("Successfully added weekly momentum");
  }

  @GetMapping("/all")
  public ResponseEntity<List<BasicResponseDto>> getWeeklyMomentumForAll() {
    List<BasicResponseDto> momentumList = weeklyMomentumService.getWeeklyMomentumForAll();
    return ResponseEntity.ok(momentumList);
  }
}
