package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cost Sales Tracker entity
 * Maps to cost_sales_tracker table
 */
@Entity
@Table(name = "cost_sales_tracker")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostSalesTracker {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_id_seq")
    @SequenceGenerator(name = "transaction_id_seq", sequenceName = "transaction_id_seq", allocationSize = 500)
    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "direction", nullable = false, length = 3)
    private String direction; // IN or OUT

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "total", nullable = false)
    private Integer total;

    @Column(name = "average_cost_per_unit", nullable = false, precision = 10, scale = 2)
    private BigDecimal averageCostPerUnit;

    @Column(name = "cost_per_unit", precision = 10, scale = 2)
    private BigDecimal costPerUnit;
}
