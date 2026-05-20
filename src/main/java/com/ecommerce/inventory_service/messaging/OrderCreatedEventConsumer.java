package com.ecommerce.inventory_service.messaging;

import com.ecommerce.inventory_service.domain.event.OrderCreatedEvent;
import com.ecommerce.inventory_service.exception.InsufficientStockException;
import com.ecommerce.inventory_service.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {

  private final InventoryService inventoryService;
  private static final Logger log = LoggerFactory.getLogger(OrderCreatedEventConsumer.class);

  @KafkaListener(
      topics = "order-created",
      groupId = "inventory-consumer-group",
      properties = {
          "spring.json.value.default.type=com.ecommerce.inventory_service.domain.event.OrderCreatedEvent",
          "spring.json.use.type.headers=false"
      }
  )
  public void consume(OrderCreatedEvent event) {
    log.info("Evento recebido do Kafka - orderId: {}", event.orderId());
    inventoryService.processOrder(event);
  }

  @KafkaListener(
      topics = "order-created-dlt",
      groupId = "inventory-dlt-consumer-group",
      containerFactory = "dltContainerFactory", // ← usa factory sem retry/DLT
      properties = {
          "spring.json.value.default.type=com.ecommerce.inventory_service.domain.event.OrderCreatedEvent",
          "spring.json.use.type.headers=false"
      },
      autoStartup = "true"
  )
  public void consumeDLT(OrderCreatedEvent event) {
    log.warn("Reprocessando mensagem da DLT - orderId: {}", event.orderId());
    inventoryService.processOrder(event);
  }
}
