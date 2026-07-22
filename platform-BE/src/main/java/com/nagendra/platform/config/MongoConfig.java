package com.nagendra.platform.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Slf4j
@Configuration
public class MongoConfig {

  @Value("${mongodb.connection.uri}")
  private String mongoUri;

  @Value("${mongodb.database}")
  private String databaseName;

  @Bean
  public MongoClient mongoClient() {

    log.info("Connecting Mongo URI:{} ", mongoUri);

    return MongoClients.create(mongoUri);
  }

  @Bean
  public MongoTemplate mongoTemplate(MongoClient mongoClient) {

    return new MongoTemplate(mongoClient, databaseName);
  }
}
