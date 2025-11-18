package com.jta.grocery.controller;

import com.jta.grocery.service.PointOfSaleService;
import com.jta.grocery.service.ProductLookupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * Point of Sale REST API Controller
 * Exposes POS operations
 */
@RestController
@RequestMapping("/api/pos")
@RequiredArgsConstructor
@Tag(name = "Point of Sale", description = "Point of Sale APIs")
public class PointOfSaleController {

    private final PointOfSaleService pointOfSaleService;
    private final ProductLookupService productLookupService;

    @PostMapping("/bills/{billId}/items")
    @Operation(summary = "Add item to bill", description = "Scan and add an item to a customer bill")
    public ResponseEntity<String> addItemToBill(
            @PathVariable Long billId,
            @RequestBody AddItemRequest request) {
        pointOfSaleService.addItemToBill(billId, request.getBarcode());
        return ResponseEntity.ok("Item added to bill successfully");
    }

    @PostMapping("/bills/{billId}/payment")
    @Operation(summary = "Receive payment", description = "Process payment for a customer bill")
    public ResponseEntity<PaymentResponse> receivePayment(
            @PathVariable Long billId,
            @RequestBody PaymentRequest request) {
        BigDecimal change = pointOfSaleService.receivePayment(
                billId,
                request.getPaymentType(),
                request.getAmount()
        );

        if (change == null) {
            return ResponseEntity.badRequest().body(
                    new PaymentResponse(null, "failed", "Payment processing failed")
            );
        }

        return ResponseEntity.ok(
                new PaymentResponse(change, "success", "Payment processed successfully")
        );
    }

    @GetMapping("/lookup")
    @Operation(summary = "Lookup product by barcode", description = "Lookup product details by barcode or PLU")
    public ResponseEntity<ProductLookupService.ProductLookupResult> lookupProduct(
            @RequestParam String barcode) {
        ProductLookupService.ProductLookupResult result = productLookupService.lookupBarcode(barcode);
        return ResponseEntity.ok(result);
    }

    @Data
    public static class AddItemRequest {
        private String barcode;
    }

    @Data
    public static class PaymentRequest {
        private String paymentType; // cash, cheque, creditcard, linx
        private BigDecimal amount;
    }

    @Data
    public static class PaymentResponse {
        private final BigDecimal change;
        private final String status;
        private final String message;
    }
}
