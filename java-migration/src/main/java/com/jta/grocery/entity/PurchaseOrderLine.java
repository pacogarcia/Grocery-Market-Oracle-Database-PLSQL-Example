package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Purchase Order Line entity
 * Maps to purchase_order_lines table
 */
@Entity
@Table(name = "purchase_order_lines")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "po_line_id_seq")
    @SequenceGenerator(name = "po_line_id_seq", sequenceName = "po_line_id_seq", allocationSize = 100)
    @Column(name = "po_line_id")
    private Long poLineId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price_rate", precision = 10, scale = 2)
    private BigDecimal priceRate;
}
