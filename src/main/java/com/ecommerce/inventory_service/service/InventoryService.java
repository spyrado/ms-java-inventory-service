package com.ecommerce.inventory_service.service;

import com.ecommerce.inventory_service.config.RabbitMQConfig;
import com.ecommerce.inventory_service.domain.Product;
import com.ecommerce.inventory_service.domain.enums.StockStatus;
import com.ecommerce.inventory_service.domain.event.OrderCreatedEvent;
import com.ecommerce.inventory_service.domain.event.OrderItemEvent;
import com.ecommerce.inventory_service.domain.event.StockUpdatedEvent;
import com.ecommerce.inventory_service.exception.InsufficientStockException;
import com.ecommerce.inventory_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class InventoryService {

  private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
  private final ProductRepository productRepository;
  private final RabbitTemplate rabbitTemplate;

  @Transactional
  public void processOrder(OrderCreatedEvent event) {
    for (OrderItemEvent item : event.items()) {
      Product product = this.getProductBySku(item.productId());

      if (product.getQuantity() < item.quantity()) {
        // estoque insuficiente → publica REJECTED
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE,
            RabbitMQConfig.ROUTING_KEY,
            new StockUpdatedEvent(event.orderId(), item.productId(), 0, StockStatus.REJECTED)
        );
        throw new InsufficientStockException(item.productId(), item.quantity(), product.getQuantity());
      }

      product.setQuantity(product.getQuantity() - item.quantity());
      this.updateProduct(product);
      // estoque debitado → publica APPROVED
      rabbitTemplate.convertAndSend(
          RabbitMQConfig.EXCHANGE,
          RabbitMQConfig.ROUTING_KEY,
          new StockUpdatedEvent(event.orderId(), item.productId(), item.quantity(), StockStatus.APPROVED)
      );
      log.info("Estoque debitado - SKU: {} | Quantidade: {} | Restante: {}",
          item.productId(), item.quantity(), product.getQuantity());
    }
  }

  @Cacheable(cacheNames = "stock", key = "#sku")
  private Product getProductBySku(String sku) {
    return this.productRepository.findBySku(sku)
        .orElseThrow(() -> new RuntimeException("Produto de id: " + sku + " não encontrado."));
  }


  @CacheEvict(cacheNames = "stock", key = "#product.sku")
  private void updateProduct(Product product) {
    this.productRepository.save(product);
  }
}