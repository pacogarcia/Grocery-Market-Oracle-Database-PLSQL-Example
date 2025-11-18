package com.jta.grocery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main Spring Boot Application
 * Java migration of JTA Supermarkets Oracle PL/SQL system
 */
@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
public class GroceryMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroceryMarketApplication.class, args);
    }
}
