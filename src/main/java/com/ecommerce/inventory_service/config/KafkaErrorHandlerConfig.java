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

  // ─── Factory para consumers da DLT: sem nova DLT, retry limitado ──────────
// Este factory é GENÉRICO — pode ser usado em qualquer @KafkaListener de DLT
// via containerFactory = "dltContainerFactory"
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object> dltContainerFactory(
      ConsumerFactory<String, Object> consumerFactory) {

    // reutiliza toda a configuração existente do ConsumerFactory autoconfigurável
    // (deserializers, trusted packages, bootstrap-servers, etc)
    // e sobrescreve apenas o necessário para a DLT
    Map<String, Object> props = new HashMap<>(consumerFactory.getConfigurationProperties());

    // earliest → quando o consumer da DLT for ativado pela primeira vez,
    // lê TODAS as mensagens desde o início do tópico
    // sem isso, o Kafka jogaria o offset para o fim e ignoraria mensagens pendentes
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    // substitui o JsonDeserializer depreciado pelo JacksonJsonDeserializer
    // que é o padrão no Spring Boot 4 (Jackson 3)
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);

    DefaultKafkaConsumerFactory<String, Object> dltConsumerFactory =
        new DefaultKafkaConsumerFactory<>(props);

    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(dltConsumerFactory);

    // DefaultErrorHandler SEM DeadLetterPublishingRecoverer
    // → retenta 3x com intervalo de 2 segundos
    // → se ainda falhar: loga o erro e avança o offset
    // → NÃO cria DLT da DLT (evita loop infinito de tópicos)
    factory.setCommonErrorHandler(new DefaultErrorHandler(new FixedBackOff(2000L, 3)));

    return factory;
  }
}