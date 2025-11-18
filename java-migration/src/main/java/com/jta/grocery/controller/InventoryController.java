package com.jta.grocery.controller;

import com.jta.grocery.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Inventory REST API Controller
 * Exposes inventory management operations
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory management APIs")
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/update")
    @Operation(summary = "Update inventory", description = "Add or remove items from inventory")
    public ResponseEntity<String> updateInventory(@RequestBody UpdateInventoryRequest request) {
        inventoryService.updateInventory(request.getProductId(), request.getQuantity(), request.getNewCost());
        return ResponseEntity.ok("Inventory updated successfully");
    }

    @PostMapping("/update-sales")
    @Operation(summary = "Update sales (end of day)", description = "Process end-of-day sales from sold_products table")
    public ResponseEntity<String> updateSales() {
        inventoryService.updateSales();
        return ResponseEntity.ok("End-of-day sales processed successfully");
    }

    @PostMapping("/stock-check")
    @Operation(summary = "Stock check", description = "Reconcile physical count with system inventory")
    public ResponseEntity<StockCheckResponse> stockCheck(@RequestBody StockCheckRequest request) {
        Integer inStock = inventoryService.stockCheck(
                request.getProductId(),
                request.getLocationId(),
                request.getValueCounted()
        );
        return ResponseEntity.ok(new StockCheckResponse(inStock, request.getValueCounted()));
    }

    @Data
    public static class UpdateInventoryRequest {
        private Long productId;
        private Integer quantity;
        private BigDecimal newCost;
    }

    @Data
    public static class StockCheckRequest {
        private Long productId;
        private Long locationId;
        private Integer valueCounted;
    }

    @Data
    public static class StockCheckResponse {
        private final Integer systemQuantity;
        private final Integer physicalCount;

        public Integer getDiscrepancy() {
            if (systemQuantity == null || physicalCount == null) {
                return null;
            }
            return systemQuantity - physicalCount;
        }
    }
}
