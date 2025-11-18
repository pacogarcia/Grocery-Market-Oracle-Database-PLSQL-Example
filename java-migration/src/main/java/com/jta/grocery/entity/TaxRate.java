package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Tax Rate entity
 * Maps to tax_rates table
 */
@Entity
@Table(name = "tax_rates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxRate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tax_code_seq")
    @SequenceGenerator(name = "tax_code_seq", sequenceName = "tax_code_seq", allocationSize = 1)
    @Column(name = "tax_code")
    private Long taxCode;

    @Column(name = "tax_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "description", length = 150)
    private String description;
}
