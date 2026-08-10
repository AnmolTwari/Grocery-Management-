# Grocery Shop Management System --- Development Phases

## Project Strategy

The project will be developed incrementally.

Do not attempt to build the complete application in one step.

Each phase should produce a working, testable result.

------------------------------------------------------------------------

# Phase 0 --- Project Setup

### Goal

Create the foundation of the project.

### Tasks

-   Create Git repository.
-   Create Spring Boot backend.
-   Configure Maven.
-   Configure PostgreSQL.
-   Create React frontend.
-   Configure environment-specific settings.
-   Establish initial folder structure.
-   Create basic README.
-   Verify frontend starts.
-   Verify backend starts.
-   Verify backend can connect to the database.

### Completion criteria

Both frontend and backend run locally and communicate with the
configured database.

------------------------------------------------------------------------

# Phase 1 --- Product Management

### Goal

Build the first complete business feature.

### Backend

Create:

-   Product entity.
-   Category entity if required.
-   Product repository.
-   Product service.
-   Product controller.
-   Product DTOs.
-   Validation.
-   Exception handling.

### API

``` text
GET    /api/products
GET    /api/products/{id}
POST   /api/products
PUT    /api/products/{id}
DELETE /api/products/{id}
```

### Frontend

Create:

-   Product list.
-   Add product form.
-   Edit product form.
-   Delete/deactivate action.
-   Search.
-   Basic filtering.

### Completion criteria

A user can create, view, update, search, and deactivate products.

------------------------------------------------------------------------

# Phase 2 --- Inventory Management

### Goal

Track stock accurately.

### Tasks

-   Add stock movement entity.
-   Implement stock-in operation.
-   Implement manual adjustment.
-   Track previous/new quantity.
-   Add minimum stock level.
-   Implement low-stock detection.
-   Implement out-of-stock detection.
-   Create inventory history.

### Completion criteria

The system accurately tracks stock changes and shows low/out-of-stock
products.

------------------------------------------------------------------------

# Phase 3 --- Sales

### Goal

Create the real shop sales workflow.

### Tasks

-   Create Sale entity.
-   Create SaleItem entity.
-   Create sale DTOs.
-   Create sale service.
-   Create sale API.
-   Validate product availability.
-   Calculate totals.
-   Reduce stock automatically.
-   Record stock movements.
-   Use transactions.
-   Build sales screen.
-   Build sale history.

### Completion criteria

A user can create a sale and the correct inventory reduction happens
atomically.

------------------------------------------------------------------------

# Phase 4 --- Dashboard

### Goal

Provide useful shop information at a glance.

### Dashboard data

-   Today's sales.
-   Today's revenue.
-   Estimated profit.
-   Total products.
-   Low-stock count.
-   Out-of-stock count.
-   Recent sales.

### Completion criteria

The owner can open the dashboard and understand the current shop status
quickly.

------------------------------------------------------------------------

# Phase 5 --- Authentication & Authorization

### Goal

Secure the application.

### Tasks

-   User entity.
-   Password hashing.
-   Login.
-   Authentication.
-   Authorization.
-   Roles.
-   Protected backend endpoints.
-   Protected frontend routes.
-   Owner/Admin role.
-   Staff role structure.

### Completion criteria

Unauthenticated users cannot access protected application functionality,
and staff permissions are appropriately restricted.

------------------------------------------------------------------------

# Phase 6 --- Reports

### Goal

Provide basic business reporting.

### Reports

-   Daily sales.
-   Monthly sales.
-   Revenue.
-   Estimated gross profit.
-   Best-selling products.
-   Low-stock report.

### Completion criteria

Reports return correct results and can be filtered appropriately.

------------------------------------------------------------------------

# Phase 7 --- UI/UX Improvement

### Goal

Make the system comfortable for real shop usage.

### Tasks

-   Responsive mobile layout.
-   Improve navigation.
-   Improve forms.
-   Loading states.
-   Empty states.
-   Error messages.
-   Confirmation dialogs.
-   Better tables.
-   Better dashboard presentation.
-   Accessibility improvements.

### Completion criteria

A non-technical shop owner can use the main workflows without developer
assistance.

------------------------------------------------------------------------

# Phase 8 --- Testing & Hardening

### Goal

Make the application reliable.

### Tasks

-   Unit tests.
-   Service tests.
-   Controller/API tests.
-   Validation tests.
-   Sale/stock transaction tests.
-   Security tests.
-   Error handling verification.
-   Input edge cases.
-   Negative stock prevention.
-   Duplicate data handling.

### Completion criteria

Important business workflows are covered by automated tests and manually
verified.

------------------------------------------------------------------------

# Phase 9 --- Free Deployment

### Goal

Make the application accessible to the friend.

### Tasks

-   Create production configuration.
-   Deploy React frontend.
-   Deploy Spring Boot backend.
-   Deploy PostgreSQL database.
-   Configure environment variables.
-   Configure CORS.
-   Configure HTTPS.
-   Test production APIs.
-   Test mobile access.
-   Create basic backup procedure.

### Completion criteria

The friend can access and use the application through a public URL.

------------------------------------------------------------------------

# Phase 10 --- Real-World Trial

### Goal

Give the system to the friend and collect actual feedback.

### Tasks

-   Add initial products.
-   Train the owner briefly.
-   Observe real workflows.
-   Record bugs.
-   Record requested improvements.
-   Fix critical issues.
-   Avoid adding unnecessary features.

### Completion criteria

The application is being used for real shop operations and the major
workflows are stable.

------------------------------------------------------------------------

# Phase 11 --- Future Enhancements

Only start after the core system is stable.

Possible features:

-   Barcode scanning.
-   Receipt printing.
-   WhatsApp receipts.
-   Supplier management.
-   Purchase management.
-   Customer management.
-   Udhaar/credit.
-   Expense tracking.
-   Expiry tracking.
-   Notifications.
-   Advanced reports.
-   PWA/mobile installation.
-   Cloud backup improvements.
-   Multiple stores.

Each future feature should receive its own requirements before
implementation.

------------------------------------------------------------------------

# Phase Completion Rule

Never move to the next major phase simply because code was written.

A phase should be considered complete only after:

1.  Implementation is finished.
2.  The application builds successfully.
3.  Relevant tests pass.
4.  The feature works manually.
5.  Important edge cases are checked.
6.  Existing features still work.
7.  Progress is recorded in Memory.md once that file exists.
