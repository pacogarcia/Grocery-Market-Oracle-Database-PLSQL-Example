package com.jta.grocery.repository;

import com.jta.grocery.entity.CustomerBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerBillRepository extends JpaRepository<CustomerBill, Long> {

    @Query("SELECT cb FROM CustomerBill cb WHERE cb.billId = :billId AND cb.paymentStatus = 'unpaid'")
    Optional<CustomerBill> findUnpaidBillById(@Param("billId") Long billId);
}
