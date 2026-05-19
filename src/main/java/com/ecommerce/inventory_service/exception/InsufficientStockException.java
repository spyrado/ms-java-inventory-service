package com.ecommerce.inventory_service.exception;

public class InsufficientStockException extends RuntimeException {
  public InsufficientStockException(String sku, Integer requested, Integer available) {
    super("Estoque insuficiente para SKU: %s. Solicitado: %d, Disponível: %d".formatted(sku, requested, available));
  }
}
