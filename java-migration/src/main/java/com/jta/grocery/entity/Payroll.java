package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Payroll entity
 * Maps to payroll table
 */
@Entity
@Table(name = "payroll")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payroll_id_seq")
    @SequenceGenerator(name = "payroll_id_seq", sequenceName = "payroll_id_seq", allocationSize = 500)
    @Column(name = "payroll_id")
    private Long payrollId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "hours_basic", nullable = false)
    private Integer hoursBasic;

    @Column(name = "hours_overtime")
    private Integer hoursOvertime;

    @Column(name = "hours_doubletime")
    private Integer hoursDoubletime;

    @Column(name = "basic_pay_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal basicPayRate;

    @Column(name = "nat_insurance_deduction", nullable = false, precision = 10, scale = 2)
    private BigDecimal natInsuranceDeduction;

    @Column(name = "hlt_surcharge_deduction", nullable = false, precision = 10, scale = 2)
    private BigDecimal hltSurchargeDeduction;

    @Column(name = "cola_deduction", precision = 10, scale = 2)
    private BigDecimal colaDeduction;

    @Column(name = "paye_deduction", precision = 10, scale = 2)
    private BigDecimal payeDeduction;

    @Column(name = "pension_deduction", precision = 10, scale = 2)
    private BigDecimal pensionDeduction;

    @Column(name = "health_plan_deduction", precision = 10, scale = 2)
    private BigDecimal healthPlanDeduction;

    @Column(name = "other_deduction", precision = 10, scale = 2)
    private BigDecimal otherDeduction;

    @Column(name = "date_staff_received")
    private LocalDate dateStaffReceived;

    @Column(name = "gross_pay", precision = 10, scale = 2)
    private BigDecimal grossPay;

    @Column(name = "net_pay", precision = 10, scale = 2)
    private BigDecimal netPay;

    @Column(name = "notes", length = 150)
    private String notes;
}
