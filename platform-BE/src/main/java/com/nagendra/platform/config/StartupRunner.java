package com.nagendra.platform.config;

import com.nagendra.platform.scheduler.PlatformScheduler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartupRunner {

  private final PlatformScheduler platformScheduler;

  @PostConstruct
  public void init() {

    platformScheduler.updateWeeklyScore();
  }
}
