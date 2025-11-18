# JTA Grocery Market - System Architecture

This document provides visual architecture diagrams for the Java Spring Boot migration of the JTA Supermarkets system.

---

## High-Level Architecture

```mermaid
graph TB
    subgraph "Client Layer"
        Client[API Clients<br/>Web/Mobile/POS]
        Swagger[Swagger UI<br/>API Documentation]
    end

    subgraph "Application Layer - Spring Boot"
        subgraph "Controller Layer"
            PayrollCtrl[PayrollController]
            InventoryCtrl[InventoryController]
            POSCtrl[PointOfSaleController]
            ExceptionHandler[GlobalExceptionHandler]
        end

        subgraph "Service Layer"
            ErrorSvc[ErrorHandlingService]
            PayrollSvc[PayrollService]
            InventorySvc[InventoryService]
            ProductLookupSvc[ProductLookupService]
            POSSvc[PointOfSaleService]
        end

        subgraph "Repository Layer"
            ProductRepo[ProductRepository]
            StaffRepo[StaffRepository]
            PayrollRepo[PayrollRepository]
            WorkHoursRepo[WorkHoursRepository]
            InventoryRepo[InventoryByLocationRepository]
            CostTrackerRepo[CostSalesTrackerRepository]
            BillRepo[CustomerBillRepository]
            BilledItemRepo[BilledItemRepository]
            ErrorRepo[JtaErrorRepository]
        end

        subgraph "Entity Layer (JPA)"
            Product[Product]
            Staff[Staff]
            Payroll[Payroll]
            WorkHours[WorkHours]
            Inventory[InventoryByLocation]
            CostTracker[CostSalesTracker]
            Bill[CustomerBill]
            BilledItem[BilledItem]
            JtaError[JtaError]
        end
    end

    subgraph "Data Layer"
        DB[(Oracle Database<br/>28 Tables)]
    end

    Client -->|HTTP/REST| PayrollCtrl
    Client -->|HTTP/REST| InventoryCtrl
    Client -->|HTTP/REST| POSCtrl
    Swagger -->|HTTP/REST| PayrollCtrl
    Swagger -->|HTTP/REST| InventoryCtrl
    Swagger -->|HTTP/REST| POSCtrl

    PayrollCtrl --> PayrollSvc
    InventoryCtrl --> InventorySvc
    POSCtrl --> POSSvc
    POSCtrl --> ProductLookupSvc

    PayrollSvc --> ErrorSvc
    InventorySvc --> ErrorSvc
    POSSvc --> ErrorSvc
    ProductLookupSvc --> ErrorSvc

    PayrollSvc --> PayrollRepo
    PayrollSvc --> WorkHoursRepo
    PayrollSvc --> StaffRepo

    InventorySvc --> CostTrackerRepo
    InventorySvc --> ProductRepo

    POSSvc --> BillRepo
    POSSvc --> BilledItemRepo
    POSSvc --> ProductLookupSvc

    ProductLookupSvc --> ProductRepo

    ErrorSvc --> ErrorRepo

    PayrollRepo --> Payroll
    WorkHoursRepo --> WorkHours
    StaffRepo --> Staff
    ProductRepo --> Product
    CostTrackerRepo --> CostTracker
    InventoryRepo --> Inventory
    BillRepo --> Bill
    BilledItemRepo --> BilledItem
    ErrorRepo --> JtaError

    Payroll -.->|JPA/Hibernate| DB
    WorkHours -.->|JPA/Hibernate| DB
    Staff -.->|JPA/Hibernate| DB
    Product -.->|JPA/Hibernate| DB
    CostTracker -.->|JPA/Hibernate| DB
    Inventory -.->|JPA/Hibernate| DB
    Bill -.->|JPA/Hibernate| DB
    BilledItem -.->|JPA/Hibernate| DB
    JtaError -.->|JPA/Hibernate| DB

    ExceptionHandler -.->|handles errors from| PayrollCtrl
    ExceptionHandler -.->|handles errors from| InventoryCtrl
    ExceptionHandler -.->|handles errors from| POSCtrl

    style Client fill:#e1f5ff
    style Swagger fill:#e1f5ff
    style DB fill:#ffe1e1
    style ErrorSvc fill:#fff3cd
    style ExceptionHandler fill:#fff3cd
```

---

## Detailed Component Architecture

```mermaid
graph TB
    subgraph "REST API Layer"
        direction LR
        PC["/api/payroll<br/>PayrollController"]
        IC["/api/inventory<br/>InventoryController"]
        POSC["/api/pos<br/>PointOfSaleController"]
    end

    subgraph "Business Logic Layer"
        direction TB

        subgraph "Payroll Domain"
            PS[PayrollService<br/>-----<br/>processPayroll()<br/>getHours()<br/>sundayCheck()<br/>getPayout()]
        end

        subgraph "Inventory Domain"
            IS[InventoryService<br/>-----<br/>updateInventory()<br/>updateSales()<br/>stockCheck()]
        end

        subgraph "POS Domain"
            POSS[PointOfSaleService<br/>-----<br/>addItemToBill()<br/>receivePayment()]
            PLS[ProductLookupService<br/>-----<br/>lookupBarcode()]
        end

        subgraph "Cross-Cutting"
            ES[ErrorHandlingService<br/>-----<br/>logError()<br/>showInConsole()]
        end
    end

    subgraph "Data Access Layer"
        direction LR

        subgraph "Spring Data JPA"
            PR[ProductRepository]
            SR[StaffRepository]
            PayR[PayrollRepository]
            WHR[WorkHoursRepository]
            CTR[CostSalesTrackerRepository]
            IBR[InventoryByLocationRepository]
            CBR[CustomerBillRepository]
            BIR[BilledItemRepository]
            ER[JtaErrorRepository]
            CDAR[CashierDrawerAssignmentRepository]
            SPR[SoldProductRepository]
        end
    end

    subgraph "Domain Model (JPA Entities)"
        direction LR

        subgraph "Product Entities"
            PE[Product]
            PCE[ProductCategory]
            TRE[TaxRate]
        end

        subgraph "Staff Entities"
            SE[Staff]
            JPE[JobPost]
            PayE[Payroll]
            WHE[WorkHours]
        end

        subgraph "Inventory Entities"
            IBLE[InventoryByLocation]
            CSTE[CostSalesTracker]
            SPE[SoldProduct]
        end

        subgraph "POS Entities"
            CBE[CustomerBill]
            BIE[BilledItem]
            CSE[CashierStation]
            CDAE[CashierDrawerAssignment]
        end

        subgraph "System Entities"
            JEE[JtaError]
            JTEE[JtaEvent]
            LE[Location]
        end

        subgraph "Supplier Entities"
            SuppE[Supplier]
            POE[PurchaseOrder]
            POLE[PurchaseOrderLine]
        end
    end

    PC --> PS
    IC --> IS
    POSC --> POSS
    POSC --> PLS

    PS --> ES
    IS --> ES
    POSS --> ES
    PLS --> ES

    PS --> PayR
    PS --> WHR
    PS --> SR

    IS --> CTR
    IS --> PR
    IS --> SPR

    POSS --> CBR
    POSS --> BIR
    POSS --> CDAR
    POSS --> PLS

    PLS --> PR

    ES --> ER

    PayR --> PayE
    WHR --> WHE
    SR --> SE
    PR --> PE
    CTR --> CSTE
    IBR --> IBLE
    CBR --> CBE
    BIR --> BIE
    ER --> JEE
    CDAR --> CDAE
    SPR --> SPE

    style ES fill:#fff3cd
    style PC fill:#d4edda
    style IC fill:#d4edda
    style POSC fill:#d4edda
```

---

## Payroll Service Architecture

```mermaid
graph TB
    subgraph "Payroll REST API"
        PC[PayrollController]
    end

    subgraph "PayrollService"
        direction TB
        PP[processPayroll<br/>Construct 03]
        GH[getHours<br/>Construct 02]
        SC[sundayCheck<br/>Construct 21]
        GP[getPayout<br/>Construct 22]
    end

    subgraph "Repositories"
        PayrollRepo[PayrollRepository<br/>-----<br/>findByStaffAndDateRange()<br/>deleteByStartDateAndEndDate()]
        WorkHoursRepo[WorkHoursRepository<br/>-----<br/>findDistinctStaffIdsByDateRange()<br/>findByStaffIdAndDateRange()]
        StaffRepo[StaffRepository<br/>-----<br/>findById()]
    end

    subgraph "Entities & Database"
        PayrollEntity[Payroll Entity<br/>-----<br/>payrollId, staffId<br/>startDate, endDate<br/>hoursBasic, hoursOvertime, hoursDoubletime<br/>grossPay, netPay<br/>natInsuranceDeduction, hltSurchargeDeduction]

        WorkHoursEntity[WorkHours Entity<br/>-----<br/>staffId, workDate<br/>hoursWorked]

        StaffEntity[Staff Entity<br/>-----<br/>staffId, firstName, lastName<br/>wageRate, wageInterval<br/>paymentSchedule]
    end

    PC -->|POST /process| PP
    PC -->|GET /hours/{id}| GH
    PC -->|GET /sunday-check/{id}| SC
    PC -->|GET /payout/{id}| GP

    PP -->|1. Get staff who worked| WorkHoursRepo
    PP -->|2. For each staff| GH
    PP -->|3. Get staff details| StaffRepo
    PP -->|4. Calculate pay| Calculate[Calculate Gross Pay<br/>-----<br/>basic × rate<br/>overtime × rate × 1.5<br/>doubletime × rate × 2]
    PP -->|5. Calculate deductions| Deduct[Calculate Deductions<br/>-----<br/>NatInsurance = gross × 0.132 / 3<br/>HltSurcharge = gross × 0.005]
    PP -->|6. Save payroll| PayrollRepo

    GH -->|Query hours| WorkHoursRepo
    GH -->|Separate by day| DayCheck{Is Sunday?}
    DayCheck -->|Yes| Double[doubletime += hours]
    DayCheck -->|No| Basic[basic += hours]
    Basic -->|> 40 hours?| OTCheck{Overtime?}
    OTCheck -->|Yes| OT[overtime = basic - 40<br/>basic = 40]

    SC -->|Get work days in month| WorkHoursRepo
    SC -->|Count Sundays| CountSun[Count Sunday occurrences]
    CountSun -->|< 2 Sundays?| Available{Available?}

    GP -->|Get payroll records| PayrollRepo
    GP -->|Sum all values| Sum[Sum gross, net, deductions]

    PayrollRepo -.-> PayrollEntity
    WorkHoursRepo -.-> WorkHoursEntity
    StaffRepo -.-> StaffEntity

    style PP fill:#d1ecf1
    style GH fill:#d1ecf1
    style SC fill:#d1ecf1
    style GP fill:#d1ecf1
    style Calculate fill:#fff3cd
    style Deduct fill:#fff3cd
```

---

## Inventory Service Architecture

```mermaid
graph TB
    subgraph "Inventory REST API"
        IC[InventoryController]
    end

    subgraph "InventoryService"
        direction TB
        UI[updateInventory<br/>Construct 04]
        US[updateSales<br/>Construct 15]
        StockCheck[stockCheck<br/>Construct 20]
    end

    subgraph "Repositories"
        CTRepo[CostSalesTrackerRepository<br/>-----<br/>findLatestByProductId()<br/>save()]
        ProdRepo[ProductRepository<br/>-----<br/>findById()]
        SoldProdRepo[SoldProductRepository<br/>-----<br/>findAll()<br/>deleteAll()]
    end

    subgraph "Business Logic"
        Validate{Validate Input}
        GetLatest[Get Latest Inventory Record]
        CalcAvg[Calculate New Average Cost<br/>-----<br/>Weighted Average Formula:<br/>newAvg = oldTotalCost + newTotalCost / combinedQty]
        Direction{Adding or<br/>Removing?}
        CheckNegative{Would total<br/>be negative?}
        SaveTracker[Save Cost Tracker Record]
    end

    subgraph "End of Day Processing"
        GetSold[Get All Sold Products]
        GroupByProduct[Group and Sum by Product]
        UpdateEach[For Each Product:<br/>updateInventory with negative qty]
        ClearSold[Clear sold_products Table]
    end

    subgraph "Entities"
        CostTrackerEntity[CostSalesTracker<br/>-----<br/>transactionId, productId<br/>direction IN/OUT<br/>quantity, total<br/>averageCostPerUnit<br/>costPerUnit, dateTime]

        SoldProductEntity[SoldProduct<br/>-----<br/>soldProductsId<br/>productId, quantity]
    end

    IC -->|POST /update| UI
    IC -->|POST /update-sales| US
    IC -->|POST /stock-check| StockCheck

    UI --> Validate
    Validate -->|Invalid| Error[Throw InvalidInputException]
    Validate -->|Valid| GetLatest
    GetLatest --> CTRepo
    CTRepo --> Direction

    Direction -->|Adding qty > 0| AddFlow[Add Flow]
    Direction -->|Removing qty < 0| RemoveFlow[Remove Flow]

    AddFlow --> ValidateCost{Cost > 0?}
    ValidateCost -->|No| Error
    ValidateCost -->|Yes| CalcAvg
    CalcAvg --> SaveTracker

    RemoveFlow --> CheckNegative
    CheckNegative -->|Yes| Error
    CheckNegative -->|No| SaveTracker

    SaveTracker --> CTRepo

    US --> GetSold
    GetSold --> SoldProdRepo
    SoldProdRepo --> GroupByProduct
    GroupByProduct --> UpdateEach
    UpdateEach --> UI
    UpdateEach --> ClearSold
    ClearSold --> SoldProdRepo

    StockCheck --> CTRepo
    StockCheck -->|If discrepancy| UI

    CTRepo -.-> CostTrackerEntity
    SoldProdRepo -.-> SoldProductEntity

    style UI fill:#d4edda
    style US fill:#d4edda
    style StockCheck fill:#d4edda
    style CalcAvg fill:#fff3cd
    style Error fill:#f8d7da
```

---

## Point of Sale Service Architecture

```mermaid
graph TB
    subgraph "POS REST API"
        POSC[PointOfSaleController]
    end

    subgraph "Services"
        POSService[PointOfSaleService]
        PLService[ProductLookupService<br/>Construct 13]
    end

    subgraph "POS Business Logic"
        AddItem[addItemToBill<br/>Construct 16]
        ReceivePay[receivePayment<br/>Construct 17]
    end

    subgraph "Product Lookup Logic"
        LookupBC[lookupBarcode]
        CheckType{Barcode<br/>Type?}
        StandardBC[Standard Barcode<br/>12-13 digits]
        PLU[PLU Code<br/>Starts with '2']
        ParsePLU[Parse PLU<br/>-----<br/>pluCode = substr 0,5<br/>price = substr 6,11 / 100]
    end

    subgraph "Repositories"
        BillRepo[CustomerBillRepository<br/>-----<br/>findUnpaidBillById()<br/>save()]
        BilledItemRepo[BilledItemRepository<br/>-----<br/>findByProductIdAndBillId()<br/>save()]
        ProdRepo[ProductRepository<br/>-----<br/>findByBarcode()<br/>findByPriceLookupCode()]
        CashierRepo[CashierDrawerAssignmentRepository<br/>-----<br/>save()]
    end

    subgraph "Payment Processing"
        ValidatePayment{Validate<br/>Payment Type}
        CheckStatus{Already<br/>Paid?}
        CalcChange[Calculate Change<br/>-----<br/>if amount >= tender:<br/>  change = amount - tender<br/>  status = 'paid'<br/>else:<br/>  status = 'pending']
        UpdateBill[Update Bill]
        UpdateDrawer[Update Cashier Drawer<br/>-----<br/>if cash: cashEnd += amount - change<br/>else: nonCash += amount]
        UpdateInventory[Update Inventory from Bill]
    end

    subgraph "Entities"
        BillEntity[CustomerBill<br/>-----<br/>billId, assignmentId<br/>paymentTender, paymentAmount<br/>paymentType, paymentStatus<br/>dateTimeCreated, dateTimePaid]

        BilledItemEntity[BilledItem<br/>-----<br/>billLineId, billId, productId<br/>quantity, priceRate<br/>taxCode, taxRate]

        ProductEntity[Product<br/>-----<br/>productId, productName<br/>barcode, priceLookupCode<br/>priceRate, taxCode]

        CashierEntity[CashierDrawerAssignment<br/>-----<br/>assignmentId, staffId<br/>cashAmountStart, cashAmountEnd<br/>nonCashTender]
    end

    POSC -->|POST /bills/{id}/items| AddItem
    POSC -->|POST /bills/{id}/payment| ReceivePay
    POSC -->|GET /lookup?barcode=| PLService

    AddItem -->|1. Verify bill unpaid| BillRepo
    AddItem -->|2. Lookup product| PLService
    AddItem -->|3. Check if exists| BilledItemRepo
    AddItem -->|4a. Update quantity| BilledItemRepo
    AddItem -->|4b. Add new item| BilledItemRepo
    AddItem -->|5. Update bill total| BillRepo

    PLService --> LookupBC
    LookupBC --> CheckType
    CheckType -->|Standard| StandardBC
    CheckType -->|PLU starts with '2'| PLU
    StandardBC --> ProdRepo
    PLU --> ParsePLU
    ParsePLU --> ProdRepo

    ReceivePay --> ValidatePayment
    ValidatePayment -->|Invalid| Error[Throw InvalidInputException]
    ValidatePayment -->|Valid| CheckStatus
    CheckStatus -->|Paid| Error
    CheckStatus -->|Unpaid/Pending| CalcChange
    CalcChange --> UpdateBill
    UpdateBill --> BillRepo
    UpdateBill --> UpdateDrawer
    UpdateDrawer --> CashierRepo
    UpdateDrawer --> UpdateInventory

    BillRepo -.-> BillEntity
    BilledItemRepo -.-> BilledItemEntity
    ProdRepo -.-> ProductEntity
    CashierRepo -.-> CashierEntity

    style AddItem fill:#cfe2ff
    style ReceivePay fill:#cfe2ff
    style LookupBC fill:#cfe2ff
    style ParsePLU fill:#fff3cd
    style CalcChange fill:#fff3cd
    style Error fill:#f8d7da
```

---

## Error Handling Architecture

```mermaid
graph TB
    subgraph "Controllers"
        PC[PayrollController]
        IC[InventoryController]
        POSC[PointOfSaleController]
    end

    subgraph "Services"
        PS[PayrollService]
        IS[InventoryService]
        POSS[PointOfSaleService]
        PLS[ProductLookupService]
    end

    subgraph "Error Handling"
        EHS[ErrorHandlingService<br/>Construct 01]
        GEH[GlobalExceptionHandler]
    end

    subgraph "Custom Exceptions"
        JtaEx[JtaException<br/>base class<br/>errorCode: int]
        InvalidEx[InvalidInputException<br/>errorCode: -20201<br/>user input errors]
        MissingEx[MissingDataException<br/>errorCode: -20202<br/>not found errors]
    end

    subgraph "Error Logging"
        LogError[logError<br/>-----<br/>@Transactional<br/>propagation = REQUIRES_NEW<br/>autonomous transaction]
        ShowConsole[showInConsole<br/>-----<br/>trivial errors<br/>no database log]
    end

    subgraph "Database"
        ErrorRepo[JtaErrorRepository]
        ErrorEntity[JtaError Entity<br/>-----<br/>errorId, dateTime<br/>userName, code, message]
    end

    subgraph "HTTP Response"
        ErrorResponse[ErrorResponse DTO<br/>-----<br/>errorCode<br/>message<br/>timestamp]
    end

    PS -->|logs errors| EHS
    IS -->|logs errors| EHS
    POSS -->|logs errors| EHS
    PLS -->|logs errors| EHS

    PS -->|throws| InvalidEx
    PS -->|throws| MissingEx
    IS -->|throws| InvalidEx
    POSS -->|throws| InvalidEx
    PLS -->|throws| MissingEx

    InvalidEx -->|extends| JtaEx
    MissingEx -->|extends| JtaEx

    JtaEx -->|caught by| GEH
    InvalidEx -->|caught by| GEH
    MissingEx -->|caught by| GEH

    GEH -->|returns| ErrorResponse
    ErrorResponse -->|HTTP 400| PC
    ErrorResponse -->|HTTP 404| IC
    ErrorResponse -->|HTTP 500| POSC

    EHS --> LogError
    EHS --> ShowConsole

    LogError -->|REQUIRES_NEW transaction| ErrorRepo
    ErrorRepo --> ErrorEntity

    style EHS fill:#fff3cd
    style GEH fill:#fff3cd
    style JtaEx fill:#f8d7da
    style InvalidEx fill:#f8d7da
    style MissingEx fill:#f8d7da
    style LogError fill:#d1ecf1
```

---

## Database Schema Overview

```mermaid
erDiagram
    PRODUCTS ||--o{ BILLED_ITEMS : contains
    PRODUCTS ||--o{ INVENTORY_BY_LOCATION : tracked_at
    PRODUCTS ||--o{ COST_SALES_TRACKER : tracks
    PRODUCTS ||--o{ SOLD_PRODUCTS : sold
    PRODUCTS }o--|| PRODUCT_CATEGORY : belongs_to
    PRODUCTS }o--|| TAX_RATES : has

    STAFF ||--o{ PAYROLL : receives
    STAFF ||--o{ WORK_HOURS : works
    STAFF }o--|| JOB_POSTS : has
    STAFF }o--|| LOCATIONS : works_at
    STAFF ||--o{ CASHIER_DRAWER_ASSIGNMENTS : assigned_to

    CUSTOMER_BILLS ||--o{ BILLED_ITEMS : contains
    CUSTOMER_BILLS }o--|| CASHIER_DRAWER_ASSIGNMENTS : created_during

    CASHIER_DRAWER_ASSIGNMENTS }o--|| CASHIER_STATIONS : uses
    CASHIER_STATIONS }o--|| LOCATIONS : located_at

    SUPPLIERS ||--o{ PURCHASE_ORDERS : receives
    PURCHASE_ORDERS ||--o{ PURCHASE_ORDER_LINES : contains
    PURCHASE_ORDER_LINES }o--|| PRODUCTS : orders

    INVENTORY_BY_LOCATION }o--|| LOCATIONS : at

    PRODUCTS {
        number product_id PK
        number category_id FK
        string product_name
        number barcode UK
        number price_lookup_code UK
        number tax_code FK
        decimal price_rate
    }

    STAFF {
        number staff_id PK
        number location_id FK
        number job_id FK
        string first_name
        string last_name
        decimal wage_rate
    }

    PAYROLL {
        number payroll_id PK
        number staff_id FK
        date start_date
        date end_date
        number hours_basic
        number hours_overtime
        number hours_doubletime
        decimal gross_pay
        decimal net_pay
    }

    CUSTOMER_BILLS {
        number bill_id PK
        number assignment_id FK
        datetime date_time_created
        decimal payment_tender
        string payment_type
        string payment_status
    }

    BILLED_ITEMS {
        number bill_line_id PK
        number bill_id FK
        number product_id FK
        number quantity
        decimal price_rate
        decimal tax_rate
    }

    COST_SALES_TRACKER {
        number transaction_id PK
        number product_id FK
        string direction
        datetime date_time
        number quantity
        number total
        decimal average_cost_per_unit
    }
```

---

## Transaction Management

```mermaid
sequenceDiagram
    participant Client
    participant Controller
    participant Service
    participant ErrorService
    participant Repository
    participant Database

    Client->>Controller: HTTP Request
    Controller->>Service: Call business method
    activate Service
    Note over Service: @Transactional<br/>Transaction begins

    Service->>Repository: Query/Save entity
    Repository->>Database: JDBC call
    Database-->>Repository: Result
    Repository-->>Service: Entity

    alt Business Logic Success
        Service->>Repository: Save changes
        Repository->>Database: UPDATE/INSERT
        Database-->>Repository: Success
        Note over Service: Transaction commits
        Service-->>Controller: Success response
    else Business Logic Error
        Service->>ErrorService: Log error
        activate ErrorService
        Note over ErrorService: @Transactional<br/>REQUIRES_NEW<br/>Independent transaction
        ErrorService->>Repository: Save error log
        Repository->>Database: INSERT into jta_errors
        Note over ErrorService: Error log commits
        deactivate ErrorService
        Note over Service: Original transaction rollback
        Service-->>Controller: Throw exception
    end

    deactivate Service
    Controller-->>Client: HTTP Response
```

---

## Deployment Architecture

```mermaid
graph TB
    subgraph "Client Tier"
        Browser[Web Browser]
        Mobile[Mobile App]
        POS[POS Terminal]
    end

    subgraph "Load Balancer"
        LB[Nginx / AWS ALB]
    end

    subgraph "Application Tier"
        App1[Spring Boot Instance 1<br/>Port 8080]
        App2[Spring Boot Instance 2<br/>Port 8080]
        App3[Spring Boot Instance 3<br/>Port 8080]
    end

    subgraph "Connection Pool"
        HikariCP1[HikariCP Pool<br/>Max: 10 connections]
        HikariCP2[HikariCP Pool<br/>Max: 10 connections]
        HikariCP3[HikariCP Pool<br/>Max: 10 connections]
    end

    subgraph "Database Tier"
        OracleDB[(Oracle Database<br/>JTA Schema<br/>28 Tables)]
    end

    subgraph "Monitoring"
        Actuator[Spring Boot Actuator<br/>Health Checks<br/>Metrics]
        Logs[Application Logs<br/>SLF4J / Logback]
    end

    Browser -->|HTTPS| LB
    Mobile -->|HTTPS| LB
    POS -->|HTTPS| LB

    LB -->|Round Robin| App1
    LB -->|Round Robin| App2
    LB -->|Round Robin| App3

    App1 --> HikariCP1
    App2 --> HikariCP2
    App3 --> HikariCP3

    HikariCP1 -->|JDBC| OracleDB
    HikariCP2 -->|JDBC| OracleDB
    HikariCP3 -->|JDBC| OracleDB

    App1 --> Actuator
    App2 --> Actuator
    App3 --> Actuator

    App1 --> Logs
    App2 --> Logs
    App3 --> Logs

    style LB fill:#d1ecf1
    style OracleDB fill:#ffe1e1
    style Actuator fill:#fff3cd
    style Logs fill:#fff3cd
```

---

## PL/SQL to Java Migration Flow

```mermaid
graph LR
    subgraph "Original PL/SQL System"
        PLSQL[PL/SQL Packages<br/>-----<br/>jta_error<br/>jta]
        Procedures[25 Procedures/Functions<br/>-----<br/>get_hours<br/>process_payroll<br/>update_inventory<br/>lookup_barcode<br/>add_item_to_bill<br/>receive_payment<br/>etc...]
        Triggers[3 Triggers<br/>-----<br/>update_job_history<br/>email_on_inv<br/>logon/logoff]
        OracleDB[(Oracle Database<br/>Tables + Logic)]
    end

    subgraph "Migration Process"
        Analyze[Analyze PL/SQL Code]
        Design[Design Java Architecture]
        MapEntities[Map Tables to JPA Entities]
        ConvertLogic[Convert Procedures to Services]
        CreateAPI[Create REST APIs]
        Test[Test Migration]
    end

    subgraph "Java Spring Boot System"
        Entities[JPA Entities<br/>28 classes]
        Repos[Spring Data Repositories<br/>11 interfaces]
        Services[Business Services<br/>5 classes]
        Controllers[REST Controllers<br/>4 classes]
        NewDB[(Oracle Database<br/>Tables only)]
    end

    PLSQL --> Analyze
    Procedures --> Analyze
    Triggers --> Analyze
    OracleDB --> Analyze

    Analyze --> Design
    Design --> MapEntities
    MapEntities --> ConvertLogic
    ConvertLogic --> CreateAPI
    CreateAPI --> Test

    MapEntities --> Entities
    ConvertLogic --> Services
    CreateAPI --> Controllers

    Entities --> Repos
    Repos --> Services
    Services --> Controllers

    Entities -.->|JPA/Hibernate| NewDB

    style PLSQL fill:#f8d7da
    style Procedures fill:#f8d7da
    style Triggers fill:#f8d7da
    style Services fill:#d4edda
    style Controllers fill:#d4edda
    style Entities fill:#d4edda
```

---

## Key Benefits of Migration

```mermaid
mindmap
  root((Java Migration<br/>Benefits))
    Architecture
      Layered Architecture
      Separation of Concerns
      SOLID Principles
      Dependency Injection
    Development
      Strong Type Safety
      IDE Support
      Better Debugging
      Unit Testing
      Mock Objects
    Integration
      REST APIs
      JSON Responses
      HTTP Standards
      Cross-Platform
      Easy Integration
    Scalability
      Horizontal Scaling
      Stateless Services
      Connection Pooling
      Caching Support
    Maintainability
      Object-Oriented
      Code Reusability
      Clear Structure
      Documentation
    Deployment
      Platform Independent
      Containerization
      Cloud-Ready
      CI/CD Friendly
```

---

## Migration Complexity Analysis

```mermaid
graph TD
    Start[PL/SQL Constructs<br/>25 total] --> Easy[Easy to Migrate<br/>8 constructs]
    Start --> Medium[Medium Complexity<br/>12 constructs]
    Start --> Hard[Complex Migration<br/>5 constructs]

    Easy --> E1[Simple CRUD<br/>lookup_barcode]
    Easy --> E2[Calculations<br/>get_hours, payout]
    Easy --> E3[Basic Updates<br/>add_item_to_bill]

    Medium --> M1[Business Logic<br/>process_payroll]
    Medium --> M2[Inventory Management<br/>update_inventory]
    Medium --> M3[Transaction Handling<br/>receive_payment]
    Medium --> M4[Date Calculations<br/>sunday_check]

    Hard --> H1[Complex Aggregations<br/>get_profits_for]
    Hard --> H2[Cursor Processing<br/>update_sales]
    Hard --> H3[Trigger Logic<br/>email_on_inv]
    Hard --> H4[Autonomous Transactions<br/>log_error]
    Hard --> H5[Collection Types<br/>get_price_changes]

    E1 -.->|Pattern| Direct[Direct Repository<br/>Method Call]
    M1 -.->|Pattern| Service[Service with<br/>Complex Logic]
    H1 -.->|Pattern| Query[Custom JPQL<br/>Queries]
    H4 -.->|Pattern| RequiresNew[REQUIRES_NEW<br/>Transaction]

    style Easy fill:#d4edda
    style Medium fill:#fff3cd
    style Hard fill:#f8d7da
```

---

Generated: 2025-11-18
Project: JTA Grocery Market Java Migration
