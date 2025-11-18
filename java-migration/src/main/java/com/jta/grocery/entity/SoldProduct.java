package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sold Products entity (temporary table for end-of-day processing)
 * Maps to sold_products table
 */
@Entity
@Table(name = "sold_products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoldProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sold_products_seq")
    @SequenceGenerator(name = "sold_products_seq", sequenceName = "sold_products_seq", allocationSize = 500)
    @Column(name = "sold_products_id")
    private Long soldProductsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
