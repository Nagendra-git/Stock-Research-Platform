package com.nagendra.platform.config;

import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestTemplateConfig {

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {

    return builder
        .requestFactory(
            () -> {
              SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

              // Connection timeout
              factory.setConnectTimeout(Duration.ofSeconds(10));

              // Read timeout
              factory.setReadTimeout(Duration.ofSeconds(30));

              return factory;
            })
        .build();
  }
}
