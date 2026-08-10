# Grocery Shop Management System --- Architecture

## 1. Architecture Goal

Build a maintainable Java Full Stack application using a clear
separation between frontend, backend, database, and deployment
infrastructure.

The application should start simple but be structured so it can grow
without requiring a complete rewrite.

## 2. Technology Stack

### Frontend

-   React.
-   JavaScript initially.
-   HTML5.
-   CSS.
-   Fetch API or Axios only if justified.
-   React Router when multiple routes require client-side routing.

### Backend

-   Java.
-   Spring Boot.
-   Spring Web.
-   Spring Data JPA.
-   Hibernate.
-   Bean Validation.
-   Spring Security when authentication is implemented.
-   Maven for build/dependency management.

### Database

Preferred initial production database:

-   PostgreSQL.

Development may use PostgreSQL locally.

The application should avoid database-specific logic where practical so
that moving between MySQL and PostgreSQL remains manageable.

### Development tools

-   IntelliJ IDEA or VS Code.
-   Git.
-   GitHub.
-   Postman or equivalent API testing tool.

## 3. High-Level Architecture

``` text
                Browser / Mobile Browser
                         |
                         v
                  React Frontend
                         |
                      HTTP/JSON
                         |
                         v
                Spring Boot REST API
                         |
          +--------------+--------------+
          |              |              |
      Controller       Service       Validation
                         |
                         v
                  Repository Layer
                         |
                         v
                 JPA / Hibernate
                         |
                         v
                    PostgreSQL
```

## 4. Backend Layering

Use this general flow:

``` text
Controller
    |
    v
Service
    |
    v
Repository
    |
    v
Database
```

### Controller

Responsible for:

-   HTTP endpoints.
-   Request/response handling.
-   Input binding.
-   Triggering validation.
-   Returning appropriate HTTP status codes.

Controllers should not contain business logic.

### Service

Responsible for:

-   Business rules.
-   Calculations.
-   Transaction boundaries.
-   Coordinating multiple repositories.
-   Stock and sales logic.

### Repository

Responsible for:

-   Database access.
-   Queries.
-   Persistence.

### Entity

Represents persisted database data.

Entities should not automatically be exposed directly as the public API
contract.

### DTO

Use DTOs for API request/response boundaries where appropriate.

## 5. Suggested Backend Package Structure

``` text
backend/
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com.example.grocery/
    │   │       ├── GroceryApplication.java
    │   │       ├── config/
    │   │       ├── controller/
    │   │       ├── dto/
    │   │       │   ├── product/
    │   │       │   ├── sale/
    │   │       │   └── auth/
    │   │       ├── entity/
    │   │       ├── exception/
    │   │       ├── repository/
    │   │       ├── security/
    │   │       └── service/
    │   └── resources/
    │       ├── application.properties
    │       └── application-{profile}.properties
    └── test/
```

Package names should be changed from `com.example.grocery` to the actual
project package.

## 6. Suggested Frontend Structure

``` text
frontend/
└── src/
    ├── assets/
    ├── components/
    ├── layouts/
    ├── pages/
    │   ├── auth/
    │   ├── dashboard/
    │   ├── products/
    │   ├── inventory/
    │   ├── sales/
    │   └── reports/
    ├── services/
    ├── hooks/
    ├── utils/
    ├── routes/
    ├── App.jsx
    └── main.jsx
```

Avoid creating unnecessary folders until there is a real need.

## 7. Initial Domain Model

Core entities:

``` text
User
Product
Category
Sale
SaleItem
StockMovement
```

Possible future entities:

``` text
Supplier
Purchase
PurchaseItem
Customer
CreditTransaction
Expense
```

## 8. Main Relationships

Conceptually:

``` text
Category
   |
   | 1
   |
   | *
 Product
   |
   | 1
   |
   | *
 SaleItem
   |
   | *
   |
   | 1
 Sale
```

A sale contains multiple sale items.

A sale item references one product.

A product can appear in many sale items.

Stock movements reference the affected product.

## 9. API Structure

Use a consistent `/api` prefix.

Initial examples:

``` text
GET    /api/products
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}

GET    /api/categories
POST   /api/categories

POST   /api/inventory/stock-in
POST   /api/inventory/adjustment
GET    /api/inventory/movements

POST   /api/sales
GET    /api/sales
GET    /api/sales/{id}

GET    /api/dashboard/summary
GET    /api/reports/sales
```

Authentication endpoints will be added when the authentication phase
begins.

## 10. Database Design Principles

-   Use generated primary keys where appropriate.
-   Use foreign keys for relationships.
-   Use constraints for basic data integrity.
-   Use indexes for frequently searched/filtering fields.
-   Use timestamps for important records.
-   Do not store calculated values unnecessarily.
-   Use decimal/numeric database types for money.
-   Preserve historical sales even if a product is later deactivated.

## 11. Transactions

Sale creation must be transactional.

Conceptually:

``` text
Create Sale
    |
    +-- Validate products
    |
    +-- Validate stock
    |
    +-- Create Sale
    |
    +-- Create Sale Items
    |
    +-- Reduce Stock
    |
    +-- Create Stock Movements
    |
    +-- Commit
```

If any important operation fails, the transaction should roll back.

## 12. Error Handling

Use centralized exception handling in Spring Boot.

API errors should follow a consistent structure, for example:

``` json
{
  "timestamp": "...",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Quantity must be greater than zero",
  "path": "/api/sales"
}
```

Do not expose stack traces or internal database details to users.

## 13. Deployment Architecture

### Initial free deployment

``` text
React
  |
  v
Vercel or equivalent static hosting

Spring Boot
  |
  v
Render or equivalent free-tier service

PostgreSQL
  |
  v
Supabase or equivalent free-tier database
```

Exact providers may be changed after checking current pricing and
availability.

### Future low-cost deployment

``` text
Domain
   |
   v
Reverse Proxy / HTTPS
   |
   v
VPS
   |
   +-- Spring Boot
   |
   +-- PostgreSQL
```

Frontend can remain on static hosting if practical.

## 14. Environments

Use separate configuration for:

-   Local development.
-   Test/development deployment.
-   Production.

Never hardcode:

-   Database passwords.
-   JWT secrets.
-   API keys.
-   Production credentials.

## 15. Architecture Principles

-   Keep controllers thin.
-   Put business logic in services.
-   Keep repositories focused on persistence.
-   Validate at API boundaries.
-   Use DTOs where they improve separation.
-   Use transactions for multi-step business operations.
-   Prefer simple solutions over premature abstraction.
-   Do not add infrastructure that the shop does not need.
