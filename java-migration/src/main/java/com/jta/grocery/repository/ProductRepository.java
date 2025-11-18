package com.jta.grocery.repository;

import com.jta.grocery.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByBarcode(Long barcode);
    Optional<Product> findByPriceLookupCode(Integer priceLookupCode);
}
