package com.ecommerce.inventory_service.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaErrorHandlerConfig {

  // ─── Bean principal: retry + DLT para consumers normais ───────────────────
  @Bean
  public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
    // após 3 tentativas manda para a DLT automaticamente
    DeadLetterPublishingRecoverer recoverer =
        new DeadLetterPublishingRecoverer(kafkaTemplate);

    // 3 tentativas com 2 segundos de intervalo
    FixedBackOff backOff = new FixedBackOff(2000L, 3);

    return new DefaultErrorHandler(recoverer, backOff);
  }

  // ─── Factory para consumers da DLT: sem retry, sem nova DLT ───────────────
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object> dltContainerFactory(
      ConsumerFactory<String, Object> consumerFactory) {

    // reutiliza toda a config existente e só muda o auto-offset-reset
    Map<String, Object> props = new HashMap<>(consumerFactory.getConfigurationProperties());
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);

    DefaultKafkaConsumerFactory<String, Object> dltConsumerFactory =
        new DefaultKafkaConsumerFactory<>(props);

    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(dltConsumerFactory);
    // sem DefaultErrorHandler → não cria DLT da DLT
    return factory;
  }
}