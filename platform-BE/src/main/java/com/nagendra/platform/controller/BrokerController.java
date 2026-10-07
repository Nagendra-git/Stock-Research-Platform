package com.nagendra.platform.controller;

import com.nagendra.platform.service.BrokerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/broker")
public class BrokerController {

  private final BrokerService brokerService;

  @GetMapping("/get-account-info")
  public String getAccountInfo() {
    return brokerService.getAccountInfo();
  }
}
