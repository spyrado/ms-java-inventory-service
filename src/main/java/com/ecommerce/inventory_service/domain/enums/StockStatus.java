package com.ecommerce.inventory_service.domain.enums;

public enum StockStatus {
  APPROVED("APPROVED"),
  REJECTED("REJECTED");

  private final String value;

  StockStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
