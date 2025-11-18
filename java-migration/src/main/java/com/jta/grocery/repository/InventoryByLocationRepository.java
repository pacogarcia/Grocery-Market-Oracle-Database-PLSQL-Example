package com.jta.grocery.repository;

import com.jta.grocery.entity.InventoryByLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryByLocationRepository extends
        JpaRepository<InventoryByLocation, InventoryByLocation.InventoryByLocationId> {

    @Query("SELECT i FROM InventoryByLocation i WHERE i.reorderLevel <= (i.minStockLevel - i.quantity)")
    List<InventoryByLocation> findItemsNeedingRestock();

    @Query("SELECT i FROM InventoryByLocation i " +
           "WHERE i.reorderLevel <= (i.minStockLevel - i.quantity) " +
           "AND i.location.locationId = :locationId")
    List<InventoryByLocation> findItemsNeedingRestockByLocation(@Param("locationId") Long locationId);
}
