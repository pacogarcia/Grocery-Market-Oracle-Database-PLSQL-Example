package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Customer Bill entity
 * Maps to customer_bills table
 */
@Entity
@Table(name = "customer_bills")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBill {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bill_id_seq")
    @SequenceGenerator(name = "bill_id_seq", sequenceName = "bill_id_seq", allocationSize = 500)
    @Column(name = "bill_id")
    private Long billId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id", nullable = false)
    private CashierDrawerAssignment cashierAssignment;

    @Column(name = "date_time_created", nullable = false)
    private LocalDateTime dateTimeCreated;

    @Column(name = "date_time_paid")
    private LocalDateTime dateTimePaid;

    @Column(name = "payment_tender", precision = 10, scale = 2)
    private BigDecimal paymentTender;

    @Column(name = "payment_amount", precision = 10, scale = 2)
    private BigDecimal paymentAmount;

    @Column(name = "payment_type", length = 10)
    private String paymentType; // cash, cheque, creditcard, linx

    @Column(name = "payment_status", nullable = false, length = 10)
    private String paymentStatus; // paid, pending, unpaid
}
