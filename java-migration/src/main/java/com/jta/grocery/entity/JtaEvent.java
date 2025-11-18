package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity for system events (login/logout tracking)
 * Maps to jta_events table
 */
@Entity
@Table(name = "jta_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JtaEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_seq")
    @SequenceGenerator(name = "event_seq", sequenceName = "event_seq", allocationSize = 10)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "user_name", length = 255)
    private String userName;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "event", nullable = false, length = 255)
    private String event;

    @Column(name = "ip_address", length = 20)
    private String ipAddress;
}
