package com.jta.grocery.service;

import com.jta.grocery.entity.Product;
import com.jta.grocery.entity.TaxRate;
import com.jta.grocery.exception.MissingDataException;
import com.jta.grocery.repository.ProductRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Product Lookup Service
 * Migrated from PL/SQL lookup_barcode procedure (Construct 13)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductLookupService {

    private final ProductRepository productRepository;
    private final ErrorHandlingService errorHandlingService;

    /**
     * Lookup product by barcode or PLU
     * Migrated from PL/SQL lookup_barcode procedure (Construct 13)
     *
     * Supports both standard barcodes and Price Lookup (PLU) codes
     * PLU format: 2|XXXX|C|PPPPP|C (prefix 2, ID, check digit, price, check digit)
     * Example: 212340012062 = beef at $12.06
     *
     * @param barcode Barcode or PLU string
     * @return Product lookup result
     */
    public ProductLookupResult lookupBarcode(String barcode) {
        try {
            if (barcode == null || barcode.isEmpty()) {
                throw new MissingDataException("Barcode cannot be null or empty");
            }

            // Check if this is a PLU (starts with '2')
            if (barcode.startsWith("2")) {
                return lookupPLU(barcode);
            } else {
                return lookupStandardBarcode(barcode);
            }

        } catch (MissingDataException e) {
            errorHandlingService.logError(e.getErrorCode(), e.getMessage());
            return new ProductLookupResult(null, null, null, null, null);
        } catch (Exception e) {
            errorHandlingService.logError(-20000, "Error in barcode lookup: " + e.getMessage(), e);
            return new ProductLookupResult(null, null, null, null, null);
        }
    }

    /**
     * Lookup standard barcode
     */
    private ProductLookupResult lookupStandardBarcode(String barcode) {
        try {
            Long barcodeValue = Long.parseLong(barcode);

            Product product = productRepository.findByBarcode(barcodeValue)
                    .orElseThrow(() -> new MissingDataException("Product not found for barcode: " + barcode));

            TaxRate taxRate = product.getTaxRate();

            return new ProductLookupResult(
                    product.getProductId(),
                    product.getProductName(),
                    product.getPriceRate(),
                    taxRate.getTaxCode(),
                    taxRate.getTaxRate()
            );

        } catch (NumberFormatException e) {
            throw new MissingDataException("Invalid barcode format: " + barcode);
        }
    }

    /**
     * Lookup PLU (Price Lookup Code)
     * PLU format: 2XXXXCPPPPPC
     *   2 = prefix
     *   XXXX = product ID (4 digits)
     *   C = check digit
     *   PPPPP = price in cents (5 digits)
     *   C = check digit
     *
     * Example: 212340012062
     *   - PLU ID: 21234 (first 5 chars)
     *   - Price: 12.06 (chars 7-11: 01206 / 100)
     */
    private ProductLookupResult lookupPLU(String plu) {
        if (plu.length() < 12) {
            throw new MissingDataException("Invalid PLU format: " + plu);
        }

        try {
            // Extract PLU code (first 5 characters)
            String pluCode = plu.substring(0, 5);
            Integer pluValue = Integer.parseInt(pluCode);

            // Extract price from barcode (positions 7-11, then divide by 100)
            String priceStr = plu.substring(6, 11);
            BigDecimal price = new BigDecimal(priceStr)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            // Find product by PLU
            Product product = productRepository.findByPriceLookupCode(pluValue)
                    .orElseThrow(() -> new MissingDataException(
                            "Product not found for PLU: " + pluCode));

            TaxRate taxRate = product.getTaxRate();

            return new ProductLookupResult(
                    product.getProductId(),
                    product.getProductName(),
                    price, // Use price from PLU instead of product price
                    taxRate.getTaxCode(),
                    taxRate.getTaxRate()
            );

        } catch (NumberFormatException e) {
            throw new MissingDataException("Invalid PLU format: " + plu);
        }
    }

    @Data
    public static class ProductLookupResult {
        private final Long productId;
        private final String productName;
        private final BigDecimal priceRate;
        private final Long taxCode;
        private final BigDecimal taxRate;
    }
}
