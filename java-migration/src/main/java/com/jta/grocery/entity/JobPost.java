package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Job Post entity
 * Maps to job_posts table
 */
@Entity
@Table(name = "job_posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPost {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "job_id_seq")
    @SequenceGenerator(name = "job_id_seq", sequenceName = "job_id_seq", allocationSize = 1)
    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "job_title", nullable = false, unique = true, length = 20)
    private String jobTitle;

    @Column(name = "starting_wage", nullable = false, precision = 10, scale = 2)
    private BigDecimal startingWage;

    @Column(name = "default_wage_interval", nullable = false, length = 10)
    private String defaultWageInterval; // hour, flat, day

    @Column(name = "default_payment_schedule", nullable = false, length = 10)
    private String defaultPaymentSchedule; // week, fortnight, month
}
