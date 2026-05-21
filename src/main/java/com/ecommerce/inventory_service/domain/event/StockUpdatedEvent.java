package com.ecommerce.inventory_service.domain.event;

import com.ecommerce.inventory_service.domain.enums.StockStatus;

import java.util.UUID;

public record StockUpdatedEvent(
    UUID orderId,
    String productId,
    Integer quantityDeduced,
    StockStatus status
) {
}
