package com.ecommerce.inventory_service.service;

import com.ecommerce.inventory_service.domain.Product;
import com.ecommerce.inventory_service.domain.event.OrderCreatedEvent;
import com.ecommerce.inventory_service.domain.event.OrderItemEvent;
import com.ecommerce.inventory_service.exception.InsufficientStockException;
import com.ecommerce.inventory_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class InventoryService {

  private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
  private final ProductRepository productRepository;

  @Transactional
  public void processOrder(OrderCreatedEvent event) {
    if (true) {
      throw new RuntimeException("DEU ERRO AQUI RAPAI asdasdasdsdZ");
    }
    for (OrderItemEvent item : event.items()) {
      Product product = this.productRepository.findBySku(item.productId()).orElseThrow(() -> new RuntimeException("Produto de id: " + item.productId() + " não encontrado."));

      if (product.getQuantity() < item.quantity()) {
        throw new InsufficientStockException(item.productId(), item.quantity(), product.getQuantity());
      }

      product.setQuantity(product.getQuantity() - item.quantity());
      this.productRepository.save(product);
      log.info("Estoque debitado - SKU: {} | Quantidade: {} | Restante: {}",
          item.productId(), item.quantity(), product.getQuantity());
    }
  }
}