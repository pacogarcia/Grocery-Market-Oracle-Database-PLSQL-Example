package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cashier Drawer Assignment entity
 * Maps to cashier_drawer_assignments table
 */
@Entity
@Table(name = "cashier_drawer_assignments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashierDrawerAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assignment_id_seq")
    @SequenceGenerator(name = "assignment_id_seq", sequenceName = "assignment_id_seq", allocationSize = 100)
    @Column(name = "assignment_id")
    private Long assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private CashierStation station;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "drawer_id", nullable = false)
    private Integer drawerId;

    @Column(name = "cash_amount_start", nullable = false, precision = 10, scale = 2)
    private BigDecimal cashAmountStart;

    @Column(name = "cash_amount_end", nullable = false, precision = 10, scale = 2)
    private BigDecimal cashAmountEnd;

    @Column(name = "non_cash_tender", precision = 10, scale = 2)
    private BigDecimal nonCashTender;
}
