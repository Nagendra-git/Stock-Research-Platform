package com.nagendra.platform.controller;

import com.nagendra.platform.dto.client.MarketQuoteResponse;
import com.nagendra.platform.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/company")
public class CompanyController {

  private final CompanyService companyService;

  @GetMapping
  public ResponseEntity<MarketQuoteResponse> getStocksInfo() {
    return new ResponseEntity<>(companyService.getStockInfo(), HttpStatus.OK);
  }
}
