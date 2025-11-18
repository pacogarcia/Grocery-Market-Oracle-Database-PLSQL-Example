package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Purchase Order entity
 * Maps to purchase_orders table
 */
@Entity
@Table(name = "purchase_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "po_id_seq")
    @SequenceGenerator(name = "po_id_seq", sequenceName = "po_id_seq", allocationSize = 100)
    @Column(name = "po_id")
    private Long poId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "pending", nullable = false, length = 1)
    private Character pending; // T or F

    @Column(name = "approved", nullable = false, length = 1)
    private Character approved; // T or F

    @Column(name = "submitted_date", nullable = false)
    private LocalDate submittedDate;

    @Column(name = "date_approved")
    private LocalDate dateApproved;
}
