package com.ecommerce.inventory_service.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Table(name = "products")
@Entity
@Getter
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  @Setter
  private String name;

  @Column(nullable = false, unique = true)
  @Setter
  private String sku;

  @Column(nullable = false)
  @Setter
  private Integer quantity;

}
