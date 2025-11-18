package com.jta.grocery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Work Hours entity
 * Maps to work_hours table
 */
@Entity
@Table(name = "work_hours")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(WorkHours.WorkHoursId.class)
public class WorkHours {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;

    @Id
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "hours_worked", nullable = false)
    private Integer hoursWorked;

    // Composite key class
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkHoursId implements Serializable {
        private Long staff;
        private LocalDate workDate;
    }
}
