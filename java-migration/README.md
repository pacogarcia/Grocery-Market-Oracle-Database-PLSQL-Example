# JTA Grocery Market - Java Migration

This is a Java Spring Boot migration of the JTA Supermarkets Oracle PL/SQL system. The application provides a modern REST API for managing grocery retail operations including payroll, inventory, point of sale, and financial tracking.

## Overview

This project migrates 25+ PL/SQL stored procedures and functions from the original Oracle database system to a Java Spring Boot application with JPA/Hibernate for database access.

### Technology Stack

- **Java 17** - Modern LTS version
- **Spring Boot 3.2.0** - Application framework
- **Spring Data JPA** - Database access layer
- **Hibernate** - ORM implementation
- **Oracle JDBC Driver** - Database connectivity
- **HikariCP** - Connection pooling
- **Lombok** - Boilerplate reduction
- **SpringDoc OpenAPI** - API documentation
- **Maven** - Dependency management

## Project Structure

```
java-migration/
├── src/main/java/com/jta/grocery/
│   ├── GroceryMarketApplication.java     # Main application class
│   ├── entity/                           # JPA entities (28 tables)
│   │   ├── Product.java
│   │   ├── Staff.java
│   │   ├── CustomerBill.java
│   │   └── ...
│   ├── repository/                       # Spring Data repositories
│   │   ├── ProductRepository.java
│   │   ├── PayrollRepository.java
│   │   └── ...
│   ├── service/                          # Business logic (migrated from PL/SQL)
│   │   ├── ErrorHandlingService.java    # Construct 01
│   │   ├── PayrollService.java          # Constructs 02, 03, 21, 22
│   │   ├── InventoryService.java        # Constructs 04, 05, 15, 20
│   │   ├── ProductLookupService.java    # Construct 13
│   │   └── PointOfSaleService.java      # Constructs 16, 17
│   ├── controller/                       # REST API controllers
│   │   ├── PayrollController.java
│   │   ├── InventoryController.java
│   │   └── PointOfSaleController.java
│   └── exception/                        # Custom exceptions
│       ├── JtaException.java
│       ├── InvalidInputException.java
│       └── MissingDataException.java
└── src/main/resources/
    ├── application.yml                   # Main configuration
    └── application-test.yml              # Test configuration
```

## Migration Details

### Migrated PL/SQL Constructs

The following PL/SQL procedures and functions have been migrated to Java:

| Construct | PL/SQL Name | Java Service | Description |
|-----------|-------------|--------------|-------------|
| 01 | jta_error package | ErrorHandlingService | Error logging and exception handling |
| 02 | get_hours | PayrollService.getHours() | Calculate work hours breakdown |
| 03 | process_payroll | PayrollService.processPayroll() | Generate weekly payroll |
| 04 | update_inventory | InventoryService.updateInventory() | Add/remove inventory items |
| 05 | restock_urgent | - | Generate purchase orders (TODO) |
| 13 | lookup_barcode | ProductLookupService.lookupBarcode() | Barcode/PLU scanning |
| 15 | update_sales | InventoryService.updateSales() | End-of-day sales processing |
| 16 | add_item_to_bill | PointOfSaleService.addItemToBill() | Add items to customer bill |
| 17 | receive_payment | PointOfSaleService.receivePayment() | Process customer payments |
| 20 | stock_check | InventoryService.stockCheck() | Physical inventory reconciliation |
| 21 | sunday_check | PayrollService.sundayCheck() | Check Sunday work availability |
| 22 | payout | PayrollService.getPayout() | Calculate employee payout |

### Key Migration Patterns

#### 1. Autonomous Transactions → REQUIRES_NEW Propagation

PL/SQL's `PRAGMA autonomous_transaction` is replaced with Spring's transaction propagation:

**PL/SQL:**
```sql
PROCEDURE log_error IS
    PRAGMA autonomous_transaction;
BEGIN
    INSERT INTO jta_errors ...;
    COMMIT;
END;
```

**Java:**
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void logError(int code, String message) {
    jtaErrorRepository.save(error);
}
```

#### 2. Cursors → Stream API / Repositories

PL/SQL cursors are replaced with JPA repositories and Java Streams:

**PL/SQL:**
```sql
CURSOR c_staff_worked IS
    SELECT DISTINCT staff_id FROM work_hours
    WHERE work_date BETWEEN v_start_date AND v_end_date;

FOR current_staff IN c_staff_worked LOOP
    -- process staff
END LOOP;
```

**Java:**
```java
List<Long> staffIds = workHoursRepository
    .findDistinctStaffIdsByDateRange(startDate, endDate);

for (Long staffId : staffIds) {
    // process staff
}
```

#### 3. OUT Parameters → Return Objects

PL/SQL OUT parameters are converted to Java return types or DTOs:

**PL/SQL:**
```sql
PROCEDURE get_hours (
    p_staff_id IN NUMBER,
    basic OUT NUMBER,
    overtime OUT NUMBER,
    doubletime OUT NUMBER
);
```

**Java:**
```java
public HoursBreakdown getHours(Long staffId, LocalDate start, LocalDate end) {
    return new HoursBreakdown(basic, overtime, doubletime);
}
```

#### 4. Exception Handling

Custom exceptions replace Oracle error codes:

**PL/SQL:**
```sql
IF p_quantity = 0 THEN
    jta_error.throw(-20201, 'cannot update a zero amount');
END IF;
```

**Java:**
```java
if (quantity == 0) {
    throw new InvalidInputException("Cannot update a zero amount to inventory");
}
```

## Setup Instructions

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Oracle Database 11g or higher (or compatible)

### Configuration

1. **Update database connection** in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@localhost:1521:XE
    username: your_username
    password: your_password
```

2. **Configure payroll rates** (if different from defaults):

```yaml
jta:
  payroll:
    nat-insurance-rate: 0.132
    hlt-surcharge-rate: 0.005
```

### Build and Run

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Or run the JAR
java -jar target/grocery-market-1.0.0-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### API Documentation

Once running, access the Swagger UI at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## API Usage Examples

### Payroll Operations

**Process Weekly Payroll:**
```bash
curl -X POST "http://localhost:8080/api/payroll/process?date=2024-01-15"
```

**Get Hours Breakdown:**
```bash
curl "http://localhost:8080/api/payroll/hours/10?startDate=2024-01-01&endDate=2024-01-07"
```

**Check Sunday Availability:**
```bash
curl "http://localhost:8080/api/payroll/sunday-check/10?month=2024-01-01"
```

### Inventory Operations

**Update Inventory:**
```bash
curl -X POST "http://localhost:8080/api/inventory/update" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 100,
    "newCost": 150.00
  }'
```

**End-of-Day Sales Processing:**
```bash
curl -X POST "http://localhost:8080/api/inventory/update-sales"
```

**Stock Check:**
```bash
curl -X POST "http://localhost:8080/api/inventory/stock-check" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "locationId": 10,
    "valueCounted": 95
  }'
```

### Point of Sale Operations

**Lookup Product:**
```bash
curl "http://localhost:8080/api/pos/lookup?barcode=1234567879111"
```

**Add Item to Bill:**
```bash
curl -X POST "http://localhost:8080/api/pos/bills/1/items" \
  -H "Content-Type: application/json" \
  -d '{
    "barcode": "1234567879111"
  }'
```

**Process Payment:**
```bash
curl -X POST "http://localhost:8080/api/pos/bills/1/payment" \
  -H "Content-Type: application/json" \
  -d '{
    "paymentType": "cash",
    "amount": 500.00
  }'
```

## Key Features

### Barcode System

The system supports both standard barcodes and custom PLU (Price Lookup) codes:

- **Standard Barcode**: 12-13 digit product identifier
  - Example: `1234567879111` (Puncheon Rum)

- **PLU Code**: Variable-priced items (produce, deli)
  - Format: `2|XXXX|C|PPPPP|C`
  - Example: `212340012062` = Product 21234 at $12.06

### Financial Accuracy

- Immutable transaction data (prices/taxes frozen at sale time)
- Weighted average cost calculation
- Comprehensive audit trail

### Payroll Rules

- First 40 hours: Regular rate
- Over 40 hours: 1.5x overtime
- Sundays: 2x double-time
- Max 2 Sundays per month per employee
- Automatic deductions (National Insurance, Health Surcharge)

## Testing

```bash
# Run unit tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Development

### Adding New Entities

1. Create entity class in `entity/` package
2. Create repository interface in `repository/` package
3. Add business logic to appropriate service
4. Expose via REST controller

### Extending Business Logic

Services follow the same patterns as the migrated PL/SQL code:
- Transaction management with `@Transactional`
- Error handling with custom exceptions
- Logging with SLF4J
- DTOs for complex return types

## Migration Status

### Completed
- ✅ Project structure and configuration
- ✅ All 28 entity classes
- ✅ Repository interfaces
- ✅ Error handling framework (Construct 01)
- ✅ Payroll services (Constructs 02, 03, 21, 22)
- ✅ Inventory services (Constructs 04, 15, 20)
- ✅ Product lookup (Construct 13)
- ✅ Point of Sale (Constructs 16, 17)
- ✅ REST API controllers
- ✅ Global exception handling

### TODO
- ⬜ Restock/Purchase Order generation (Construct 05)
- ⬜ Financial analytics (Constructs 06, 07, 10, 18, 19)
- ⬜ Tax and pricing functions (Constructs 08, 09, 11, 12)
- ⬜ Trigger logic migration (Constructs 23, 24, 25)
- ⬜ Comprehensive unit tests
- ⬜ Integration tests
- ⬜ Security (Spring Security)
- ⬜ Additional entities (tax history, price history, suppliers, etc.)

## Database Schema

The application uses the existing Oracle database schema created by `JTA_Create_Database.sql`. Ensure the schema is created before running the application.

## License

This is an educational project demonstrating PL/SQL to Java migration patterns.

## Original PL/SQL Project

This Java application is a migration of the original Oracle PL/SQL project. See the parent directory for the original PL/SQL code and database creation scripts.
