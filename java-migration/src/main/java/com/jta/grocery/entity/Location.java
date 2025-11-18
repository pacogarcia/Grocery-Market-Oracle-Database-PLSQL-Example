package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Location entity (branches and warehouses)
 * Maps to locations table
 */
@Entity
@Table(name = "locations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_id_seq")
    @SequenceGenerator(name = "location_id_seq", sequenceName = "location_id_seq", allocationSize = 1)
    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "name", nullable = false, unique = true, length = 20)
    private String name;

    @Column(name = "type", length = 20)
    private String type;

    @Column(name = "address", nullable = false, length = 150)
    private String address;
}
