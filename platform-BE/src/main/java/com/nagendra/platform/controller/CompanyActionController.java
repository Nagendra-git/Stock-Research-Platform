package com.nagendra.platform.controller;

import com.nagendra.platform.service.CompanyActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/results")
public class CompanyActionController {

  private final CompanyActionService companyActionService;

  @PostMapping("/quarterly")
  public ResponseEntity<String> addQuarterlyStocks() {

    companyActionService.addQuarterlyStocks();
    return new ResponseEntity<>("Successfully stock added", HttpStatus.OK);
  }

  @GetMapping("/get-fundamental-score")
  public ResponseEntity<String> getFundamentalScore(@RequestParam String isin) {
    companyActionService.getFundamentalScoreByIsIn(isin);
    return new ResponseEntity<>("Success", HttpStatus.OK);
  }

  @GetMapping("/get-price-returns-score")
  public ResponseEntity<String> getPriceScore(@RequestParam String isin) {
    companyActionService.getPriceScore(isin);
    return new ResponseEntity<>("Success", HttpStatus.OK);
  }
}
