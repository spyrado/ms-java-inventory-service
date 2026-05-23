package com.ecommerce.inventory_service.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

@Configuration
@EnableCaching // ← habilita o cache com @Cacheable e @CacheEvict
public class RedisConfig {

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(10)) // cache expira em 10 minutos
        .disableCachingNullValues();       // não cacheia valores nulos

    return RedisCacheManager.builder(factory)
        .cacheDefaults(config)
        .build();
  }
}