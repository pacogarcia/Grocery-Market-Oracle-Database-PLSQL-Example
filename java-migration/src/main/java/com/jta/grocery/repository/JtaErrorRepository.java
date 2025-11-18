package com.jta.grocery.repository;

import com.jta.grocery.entity.JtaError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JtaErrorRepository extends JpaRepository<JtaError, Long> {
}
