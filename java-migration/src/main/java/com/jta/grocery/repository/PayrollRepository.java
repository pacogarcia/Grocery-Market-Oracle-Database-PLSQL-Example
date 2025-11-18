package com.jta.grocery.repository;

import com.jta.grocery.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    void deleteByStartDateAndEndDate(LocalDate startDate, LocalDate endDate);

    @Query("SELECT p FROM Payroll p WHERE p.staff.staffId = :staffId " +
           "AND p.dateStaffReceived BETWEEN :beginDate AND :endDate")
    List<Payroll> findByStaffAndDateRange(@Param("staffId") Long staffId,
                                          @Param("beginDate") LocalDate beginDate,
                                          @Param("endDate") LocalDate endDate);
}
