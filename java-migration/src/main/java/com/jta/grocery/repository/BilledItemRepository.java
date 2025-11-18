package com.jta.grocery.repository;

import com.jta.grocery.entity.BilledItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BilledItemRepository extends JpaRepository<BilledItem, Long> {

    @Query("SELECT bi FROM BilledItem bi WHERE bi.product.productId = :productId " +
           "AND bi.customerBill.billId = :billId")
    Optional<BilledItem> findByProductIdAndBillId(@Param("productId") Long productId,
                                                    @Param("billId") Long billId);
}
