package com.ecommerce.inventory_service.domain.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
    UUID orderId,
    String customerId,
    List<OrderItemEvent> items,
    BigDecimal totalAmount
) {}