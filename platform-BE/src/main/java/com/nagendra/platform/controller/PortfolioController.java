package com.nagendra.platform.controller;

import com.nagendra.platform.dto.portfolio.PortfolioResponseDto;
import com.nagendra.platform.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

  private final PortfolioService portfolioService;

  @GetMapping("/summary")
  public PortfolioResponseDto getPortfolioSummary() {
    return portfolioService.getPortfolioSummary();
  }
}
