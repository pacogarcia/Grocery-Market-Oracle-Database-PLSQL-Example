package com.jta.grocery.service;

import com.jta.grocery.entity.*;
import com.jta.grocery.exception.InvalidInputException;
import com.jta.grocery.exception.MissingDataException;
import com.jta.grocery.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Inventory Service
 * Migrated from PL/SQL jta package inventory functions (Constructs 04, 05, 15, 20)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryService {

    private final CostSalesTrackerRepository costSalesTrackerRepository;
    private final ProductRepository productRepository;
    private final SoldProductRepository soldProductRepository;
    private final ErrorHandlingService errorHandlingService;

    /**
     * Update inventory
     * Migrated from PL/SQL update_inventory procedure (Construct 04)
     *
     * Adds or removes items from overall inventory and updates average cost
     *
     * @param productId Product ID
     * @param quantity Quantity (positive to add, negative to remove)
     * @param newCost Cost per unit when adding (null when removing)
     */
    @Transactional
    public void updateInventory(Long productId, Integer quantity, BigDecimal newCost) {
        try {
            // Validate quantity
            if (quantity == 0) {
                throw new InvalidInputException("Cannot update a zero amount to inventory");
            }

            // Validate product exists
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new InvalidInputException("Non-existing product being updated to inventory"));

            // Get latest inventory record
            Optional<CostSalesTracker> latestOpt = costSalesTrackerRepository.findLatestByProductId(productId);

            int oldTotal = latestOpt.map(CostSalesTracker::getTotal).orElse(0);
            BigDecimal oldAvg = latestOpt.map(CostSalesTracker::getAverageCostPerUnit).orElse(BigDecimal.ZERO);

            String direction;
            int newTotal;
            BigDecimal newAvg;
            BigDecimal cost;

            if (quantity > 0) {
                // Adding items
                if (newCost == null || newCost.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new InvalidInputException("Cannot update inventory with non-positive cost per unit");
                }

                direction = "IN";
                cost = newCost;

                // Calculate new average cost using weighted average
                BigDecimal oldTotalCost = oldAvg.multiply(BigDecimal.valueOf(oldTotal));
                BigDecimal newTotalCost = newCost.multiply(BigDecimal.valueOf(quantity));
                BigDecimal combinedCost = oldTotalCost.add(newTotalCost);
                int combinedQuantity = oldTotal + quantity;

                newAvg = combinedCost.divide(BigDecimal.valueOf(combinedQuantity), 2, RoundingMode.HALF_UP);
                newTotal = combinedQuantity;

            } else {
                // Removing items
                direction = "OUT";
                cost = null;
                newTotal = oldTotal + quantity; // quantity is negative

                if (newTotal < 0) {
                    throw new InvalidInputException("Cannot remove more items than already exists in inventory");
                }

                newAvg = oldAvg; // Average remains the same when removing
            }

            // Create new cost tracker record
            CostSalesTracker tracker = CostSalesTracker.builder()
                    .product(product)
                    .direction(direction)
                    .dateTime(LocalDateTime.now())
                    .quantity(quantity)
                    .total(newTotal)
                    .averageCostPerUnit(newAvg)
                    .costPerUnit(cost)
                    .build();

            costSalesTrackerRepository.save(tracker);

            log.debug("Updated inventory for product {}: {} -> {}", productId, oldTotal, newTotal);

        } catch (InvalidInputException e) {
            errorHandlingService.logError(e.getErrorCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            errorHandlingService.logError(-20000, "Error updating inventory: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Update sales at end of day
     * Migrated from PL/SQL update_sales procedure (Construct 15)
     *
     * Process sold_products table and update inventory
     */
    @Transactional
    public void updateSales() {
        try {
            log.info("Starting end-of-day sales update");

            // Get all sold products
            List<SoldProduct> soldProducts = soldProductRepository.findAll();

            if (soldProducts.isEmpty()) {
                log.warn("No data to update in sold_products table");
                return;
            }

            // Sum up quantities by product
            var productQuantities = soldProducts.stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                            sp -> sp.getProduct().getProductId(),
                            java.util.stream.Collectors.summingInt(SoldProduct::getQuantity)
                    ));

            // Update inventory for each product
            for (var entry : productQuantities.entrySet()) {
                Long productId = entry.getKey();
                Integer totalQuantity = entry.getValue();

                // Update inventory (negative quantity to remove)
                updateInventory(productId, -totalQuantity, null);
            }

            // Clear sold products table
            soldProductRepository.deleteAll();

            log.info("End-of-day sales update completed for {} products", productQuantities.size());

        } catch (Exception e) {
            errorHandlingService.logError(-20000, "Error updating sales: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Stock check - reconcile physical count with system
     * Migrated from PL/SQL stock_check procedure (Construct 20)
     *
     * @param productId Product ID
     * @param locationId Location ID
     * @param valueCounted Physical count
     * @return In-stock quantity before adjustment
     */
    @Transactional
    public Integer stockCheck(Long productId, Long locationId, Integer valueCounted) {
        try {
            // This would require inventory_by_location entity and repository
            // Simplified version that updates the cost_sales_tracker
            Optional<CostSalesTracker> latestOpt = costSalesTrackerRepository.findLatestByProductId(productId);

            if (latestOpt.isEmpty()) {
                errorHandlingService.showInConsole(null, "No inventory data for this item");
                return null;
            }

            int inStock = latestOpt.get().getTotal();

            if (inStock > valueCounted) {
                // There are missing items
                int difference = inStock - valueCounted;

                log.warn("Stock discrepancy for product {}: Expected {}, Counted {}. Missing: {}",
                        productId, inStock, valueCounted, difference);

                // Update inventory to reflect counted value
                updateInventory(productId, -difference, null);
            }

            return inStock;

        } catch (Exception e) {
            errorHandlingService.logError(-20000, "Error in stock check: " + e.getMessage(), e);
            return null;
        }
    }

    @Data
    public static class StockCheckResult {
        private final Integer inStock;
        private final Integer counted;
        private final Integer discrepancy;
    }
}
