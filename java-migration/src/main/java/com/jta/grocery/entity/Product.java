package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Product entity
 * Maps to products table
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_id_seq")
    @SequenceGenerator(name = "product_id_seq", sequenceName = "product_id_seq", allocationSize = 10)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    @Column(name = "unit_measure", length = 2)
    private String unitMeasure;

    @Column(name = "product_name", nullable = false, length = 50)
    private String productName;

    @Column(name = "description", length = 150)
    private String description;

    @Column(name = "barcode", unique = true)
    private Long barcode;

    @Column(name = "price_lookup_code", unique = true, precision = 5)
    private Integer priceLookupCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_code", nullable = false)
    private TaxRate taxRate;

    @Column(name = "price_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceRate;

    @Column(name = "min_stock_level")
    private Integer minStockLevel;

    @Column(name = "reorder_level")
    private Integer reorderLevel;
}
