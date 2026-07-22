package com.nagendra.platform.config;

import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component("mongoHealthIndicator") // The exact name used by Spring Boot's internal registry
@Primary // Forces Spring to prioritize this custom bean over the autoconfigured one
public class CustomMongoHealthIndicator implements HealthIndicator {

  private final MongoClient mongoClient;
  private final String targetDatabase;

  public CustomMongoHealthIndicator(
      MongoClient mongoClient, @Value("${mongodb.database}") String targetDatabase) {
    this.mongoClient = mongoClient;
    this.targetDatabase = targetDatabase;
  }

  @Override
  public Health health() {
    try {
      // Force the "hello" check command exclusively inside your permitted application database
      Document commandResult =
          mongoClient.getDatabase(targetDatabase).runCommand(new Document("hello", 1));

      if (commandResult.get("ok").equals(1.0)) {
        return Health.up()
            .withDetail("database", targetDatabase)
            .withDetail("status", "Successfully reached application database")
            .build();
      }
      return Health.down().withDetail("error", "Database returned ok = 0").build();
    } catch (Exception e) {
      return Health.down(e).withDetail("database", targetDatabase).build();
    }
  }
}
