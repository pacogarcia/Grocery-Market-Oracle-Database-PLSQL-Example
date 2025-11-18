package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Inventory By Location entity
 * Maps to inventory_by_location table
 */
@Entity
@Table(name = "inventory_by_location")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(InventoryByLocation.InventoryByLocationId.class)
public class InventoryByLocation {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "min_stock_level")
    private Integer minStockLevel;

    @Column(name = "reorder_level")
    private Integer reorderLevel;

    // Composite key class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryByLocationId implements Serializable {
        private Long product;
        private Long location;
    }
}
