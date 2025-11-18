package com.jta.grocery.service;

import com.jta.grocery.entity.*;
import com.jta.grocery.exception.InvalidInputException;
import com.jta.grocery.repository.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Point of Sale Service
 * Migrated from PL/SQL jta package POS functions (Constructs 16, 17)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PointOfSaleService {

    private final CustomerBillRepository customerBillRepository;
    private final BilledItemRepository billedItemRepository;
    private final ProductRepository productRepository;
    private final CashierDrawerAssignmentRepository cashierDrawerAssignmentRepository;
    private final SoldProductRepository soldProductRepository;
    private final ProductLookupService productLookupService;
    private final ErrorHandlingService errorHandlingService;

    /**
     * Add item to bill
     * Migrated from PL/SQL add_item_to_bill procedure (Construct 16)
     *
     * @param billId Customer bill ID
     * @param barcode Product barcode or PLU
     */
    @Transactional
    public void addItemToBill(Long billId, String barcode) {
        try {
            // Verify bill exists and is unpaid
            CustomerBill bill = customerBillRepository.findUnpaidBillById(billId)
                    .orElseThrow(() -> new InvalidInputException(
                            "Failed to update bill items because bill status not unpaid"));

            // Lookup product by barcode
            ProductLookupService.ProductLookupResult lookupResult = productLookupService.lookupBarcode(barcode);

            if (lookupResult.getProductId() == null) {
                throw new InvalidInputException("Product not found for barcode: " + barcode);
            }

            Product product = productRepository.findById(lookupResult.getProductId())
                    .orElseThrow(() -> new InvalidInputException("Product not found"));

            // Check if item already in bill
            Optional<BilledItem> existingItem = billedItemRepository.findByProductIdAndBillId(
                    product.getProductId(), billId);

            if (existingItem.isPresent()) {
                // Update quantity
                BilledItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + 1);
                billedItemRepository.save(item);
            } else {
                // Add new billed item
                BilledItem newItem = BilledItem.builder()
                        .customerBill(bill)
                        .product(product)
                        .quantity(1)
                        .priceRate(lookupResult.getPriceRate())
                        .taxCode(lookupResult.getTaxCode())
                        .taxRate(lookupResult.getTaxRate())
                        .build();
                billedItemRepository.save(newItem);
            }

            // Update bill total
            BigDecimal currentTender = bill.getPaymentTender() != null ?
                    bill.getPaymentTender() : BigDecimal.ZERO;
            bill.setPaymentTender(currentTender.add(lookupResult.getPriceRate()));
            customerBillRepository.save(bill);

            log.debug("Added item to bill {}: {} x {}", billId, product.getProductName(), lookupResult.getPriceRate());

        } catch (InvalidInputException e) {
            errorHandlingService.showInConsole(e.getErrorCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            errorHandlingService.logError(-20000, "Error adding item to bill: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Receive payment for a bill
     * Migrated from PL/SQL receive_payment function (Construct 17)
     *
     * Uses autonomous transaction (REQUIRES_NEW) to ensure payment is processed independently
     *
     * @param billId Bill ID
     * @param paymentType Payment type (cash, cheque, creditcard, linx)
     * @param amount Amount tendered
     * @return Change to return to customer (null if failed)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public BigDecimal receivePayment(Long billId, String paymentType, BigDecimal amount) {
        try {
            // Validate payment type
            if (!isValidPaymentType(paymentType)) {
                throw new InvalidInputException(
                        "Invalid payment type, valid types: cash, cheque, creditcard, linx");
            }

            // Get bill
            CustomerBill bill = customerBillRepository.findById(billId)
                    .orElseThrow(() -> new InvalidInputException("Bill not found"));

            // Check if already paid
            if ("paid".equals(bill.getPaymentStatus())) {
                throw new InvalidInputException("Attempt to pay on bill that has already received full payment");
            }

            // Validate amount
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new InvalidInputException("Invalid money amount, negative number");
            }

            BigDecimal tender = bill.getPaymentTender() != null ? bill.getPaymentTender() : BigDecimal.ZERO;
            BigDecimal previousPayment = bill.getPaymentAmount() != null ? bill.getPaymentAmount() : BigDecimal.ZERO;
            boolean updateInventory = true;

            // Adjust for pending payments
            if ("pending".equals(bill.getPaymentStatus())) {
                tender = tender.subtract(previousPayment);
                updateInventory = false;
            }

            // Calculate change and status
            BigDecimal change = BigDecimal.ZERO;
            String newStatus;

            if (amount.compareTo(tender) < 0) {
                // Partial payment - mark as pending
                newStatus = "pending";
            } else {
                // Full payment
                newStatus = "paid";
                change = amount.subtract(tender);
            }

            // Update bill
            bill.setPaymentAmount(previousPayment.add(amount).subtract(change));
            bill.setPaymentType(paymentType);
            bill.setPaymentStatus(newStatus);
            bill.setDateTimePaid(LocalDateTime.now());
            customerBillRepository.save(bill);

            // Update cashier drawer
            CashierDrawerAssignment assignment = bill.getCashierAssignment();
            if ("cash".equals(paymentType)) {
                BigDecimal cashEnd = assignment.getCashAmountEnd() != null ?
                        assignment.getCashAmountEnd() : BigDecimal.ZERO;
                assignment.setCashAmountEnd(cashEnd.add(amount).subtract(change));
            } else {
                BigDecimal nonCash = assignment.getNonCashTender() != null ?
                        assignment.getNonCashTender() : BigDecimal.ZERO;
                assignment.setNonCashTender(nonCash.add(amount));

                BigDecimal cashEnd = assignment.getCashAmountEnd() != null ?
                        assignment.getCashAmountEnd() : BigDecimal.ZERO;
                assignment.setCashAmountEnd(cashEnd.subtract(change));
            }
            cashierDrawerAssignmentRepository.save(assignment);

            // Update inventory if this is first payment
            if (updateInventory) {
                updateInventoryFromBill(billId);
            }

            log.info("Payment received for bill {}: {} {}, change: {}",
                    billId, amount, paymentType, change);

            return change;

        } catch (InvalidInputException e) {
            errorHandlingService.showInConsole(e.getErrorCode(), e.getMessage());
            return null;
        } catch (Exception e) {
            errorHandlingService.logError(-20000, "Error receiving payment: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Update inventory from bill (after payment)
     * Migrated from PL/SQL update_from_bill procedure (Construct 14)
     *
     * @param billId Bill ID
     */
    private void updateInventoryFromBill(Long billId) {
        // For each billed item, add to sold_products table
        // This will be processed at end of day by updateSales()
        // Simplified - in full implementation would also update inventory_by_location

        // This is a placeholder - full implementation would:
        // 1. Get all billed items for this bill
        // 2. For each item (if has barcode, not PLU):
        //    - Add to sold_products table
        //    - Update inventory_by_location
    }

    private boolean isValidPaymentType(String paymentType) {
        return "cash".equals(paymentType) ||
               "cheque".equals(paymentType) ||
               "creditcard".equals(paymentType) ||
               "linx".equals(paymentType);
    }

    @Data
    public static class PaymentResult {
        private final BigDecimal change;
        private final String status;
        private final String message;
    }
}
