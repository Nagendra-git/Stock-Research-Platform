package com.nagendra.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
@EnableScheduling
public class PlatformApplication {

  public static void main(String[] args) {
    SpringApplication.run(PlatformApplication.class, args);
  }
}
