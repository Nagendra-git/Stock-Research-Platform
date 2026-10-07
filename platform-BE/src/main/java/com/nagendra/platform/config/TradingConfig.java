package com.nagendra.platform.config;

import com.nagendra.platform.service.OrderExecutionService;
import com.nagendra.platform.trading.PaperOrderExecutionService;
import com.nagendra.platform.upstox.UpstoxOrderExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TradingProperties.class)
public class TradingConfig {

  private static final Logger log = LoggerFactory.getLogger(TradingConfig.class);

  /**
   * Exactly one OrderExecutionService bean exists at a time, chosen by trading.mode. Defaults to
   * PAPER (safe) - LIVE must be opted into explicitly in application.yml or via TRADING_MODE=LIVE.
   */
  @Bean
  public OrderExecutionService orderExecutionService(
      TradingProperties properties, UpstoxMarketDataProperties upstoxProperties) {
    if (properties.isLive()) {
      log.warn("*** trading.mode=LIVE - real orders will be sent to Upstox with real money ***");
      return new UpstoxOrderExecutionService(properties, upstoxProperties);
    }
    log.info("trading.mode=PAPER - orders are simulated only, nothing is sent to Upstox");
    return new PaperOrderExecutionService();
  }
}
