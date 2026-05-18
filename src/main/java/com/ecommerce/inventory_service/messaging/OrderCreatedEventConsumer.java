package com.ecommerce.inventory_service.messaging;

import com.ecommerce.inventory_service.domain.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {

  private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventConsumer.class);

  @KafkaListener(topics = "order-created", groupId = "inventory-consumer-group", properties = {
      "spring.json.value.default.type=com.ecommerce.inventory_service.domain.event.OrderCreatedEvent",
      "spring.json.use.type.headers=false"
  })
  public void consume(OrderCreatedEvent event) {
    log.info("Evento recebido do Kafka - orderId: {}", event.orderId());
  }
}
