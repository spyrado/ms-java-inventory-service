package com.ecommerce.inventory_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorHandlerConfig {

  @Bean
  public DefaultErrorHandler errorHandler() {
    // tenta 3 vezes com intervalo de 2 segundos
    return new DefaultErrorHandler(new FixedBackOff(2000L, 3));
    //                                               ↑       ↑
    //                                          intervalo  tentativas
  }
}