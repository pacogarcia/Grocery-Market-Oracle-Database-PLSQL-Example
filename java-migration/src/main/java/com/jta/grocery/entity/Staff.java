package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Staff entity
 * Maps to staff table
 */
@Entity
@Table(name = "staff")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "staff_id_seq")
    @SequenceGenerator(name = "staff_id_seq", sequenceName = "staff_id_seq", allocationSize = 1)
    @Column(name = "staff_id")
    private Long staffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobPost jobPost;

    @Column(name = "first_name", nullable = false, length = 20)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 20)
    private String lastName;

    @Column(name = "home_phone", nullable = false, length = 14)
    private String homePhone;

    @Column(name = "mobile_phone", length = 14)
    private String mobilePhone;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "address", nullable = false, length = 150)
    private String address;

    @Column(name = "dob", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "emergency_number", length = 14)
    private String emergencyNumber;

    @Column(name = "wage_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal wageRate;

    @Column(name = "wage_interval", nullable = false, length = 10)
    private String wageInterval; // hour, flat, day

    @Column(name = "payment_schedule", nullable = false, length = 10)
    private String paymentSchedule; // week, fortnight, month

    @Column(name = "gender", nullable = false, length = 1)
    private Character gender; // M or F

    @Column(name = "is_married", nullable = false, length = 1)
    private Character isMarried; // T or F

    @Column(name = "is_active", nullable = false, length = 1)
    private Character isActive; // T or F

    @Column(name = "is_permanent", nullable = false, length = 1)
    private Character isPermanent; // T or F
}
