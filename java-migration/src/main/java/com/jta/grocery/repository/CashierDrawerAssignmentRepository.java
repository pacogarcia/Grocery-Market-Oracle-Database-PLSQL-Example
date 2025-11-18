package com.jta.grocery.repository;

import com.jta.grocery.entity.CashierDrawerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashierDrawerAssignmentRepository extends JpaRepository<CashierDrawerAssignment, Long> {
}
