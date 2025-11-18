package com.jta.grocery.repository;

import com.jta.grocery.entity.WorkHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface WorkHoursRepository extends JpaRepository<WorkHours, WorkHours.WorkHoursId> {

    @Query("SELECT DISTINCT w.staff.staffId FROM WorkHours w " +
           "WHERE w.workDate BETWEEN :startDate AND :endDate " +
           "ORDER BY w.staff.staffId")
    List<Long> findDistinctStaffIdsByDateRange(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    @Query("SELECT w FROM WorkHours w WHERE w.staff.staffId = :staffId " +
           "AND w.workDate BETWEEN :startDate AND :endDate")
    List<WorkHours> findByStaffIdAndDateRange(@Param("staffId") Long staffId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
}
