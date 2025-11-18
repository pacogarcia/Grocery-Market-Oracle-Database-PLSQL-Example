package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Billed Item entity
 * Maps to billed_items table
 */
@Entity
@Table(name = "billed_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BilledItem {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bill_line_id_seq")
    @SequenceGenerator(name = "bill_line_id_seq", sequenceName = "bill_line_id_seq", allocationSize = 500)
    @Column(name = "bill_line_id")
    private Long billLineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private CustomerBill customerBill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceRate;

    @Column(name = "tax_code", nullable = false)
    private Long taxCode;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;
}
