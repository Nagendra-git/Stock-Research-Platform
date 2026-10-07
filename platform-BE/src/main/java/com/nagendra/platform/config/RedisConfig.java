package com.nagendra.platform.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableConfigurationProperties(UpstoxMarketDataProperties.class)
public class RedisConfig {

  @Bean
  public ObjectMapper redisObjectMapper() {
    return JsonMapper.builder().build();
  }

  @Primary
  @Bean
  public RedisTemplate<String, String> marketDataRedisTemplate(
      RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, String> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    StringRedisSerializer stringSerializer = new StringRedisSerializer();
    template.setKeySerializer(stringSerializer);
    template.setHashKeySerializer(stringSerializer);
    template.setValueSerializer(stringSerializer);
    template.setHashValueSerializer(stringSerializer);
    template.afterPropertiesSet();
    return template;
  }
}
