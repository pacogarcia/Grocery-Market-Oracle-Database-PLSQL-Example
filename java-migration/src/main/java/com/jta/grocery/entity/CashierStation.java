package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cashier Station entity
 * Maps to cashier_stations table
 */
@Entity
@Table(name = "cashier_stations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CashierStation {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "station_id_seq")
    @SequenceGenerator(name = "station_id_seq", sequenceName = "station_id_seq", allocationSize = 1)
    @Column(name = "station_id")
    private Long stationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "description", length = 150)
    private String description;
}
