package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for error logging
 * Maps to jta_errors table
 */
@Entity
@Table(name = "jta_errors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JtaError {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "error_seq")
    @SequenceGenerator(name = "error_seq", sequenceName = "error_seq", allocationSize = 10)
    @Column(name = "error_id")
    private Long errorId;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "user_name", length = 255)
    private String userName;

    @Column(name = "code", length = 6)
    private String code;

    @Column(name = "message", length = 255)
    private String message;
}
