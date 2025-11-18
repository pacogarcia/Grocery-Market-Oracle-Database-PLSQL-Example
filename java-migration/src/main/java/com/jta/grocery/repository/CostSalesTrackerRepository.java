package com.jta.grocery.repository;

import com.jta.grocery.entity.CostSalesTracker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CostSalesTrackerRepository extends JpaRepository<CostSalesTracker, Long> {

    @Query("SELECT c FROM CostSalesTracker c WHERE c.product.productId = :productId " +
           "AND c.transactionId = (SELECT MAX(c2.transactionId) FROM CostSalesTracker c2 " +
           "WHERE c2.product.productId = :productId)")
    Optional<CostSalesTracker> findLatestByProductId(@Param("productId") Long productId);
}
