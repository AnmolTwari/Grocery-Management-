# Grocery Shop Management System --- Product Requirements Document

## 1. Project Overview

The Grocery Shop Management System is a web-based Java Full Stack
application designed for a small grocery/retail shop.

The primary goal is to help the shop owner manage products, inventory,
sales, stock levels, and basic business reports from a simple interface
that works well on both desktop and mobile devices.

This is a real-world project intended to be usable by a small shop, not
only a demonstration CRUD application.

## 2. Project Goals

### Primary goals

-   Manage grocery products digitally.
-   Track current stock accurately.
-   Record sales and automatically reduce inventory.
-   Identify low-stock and out-of-stock products.
-   Track purchase and selling prices.
-   Provide basic sales and profit reports.
-   Make the application simple enough for a non-technical shop owner.
-   Deploy the first usable version at zero cost where practical.
-   Keep the architecture maintainable so the application can later move
    to paid hosting/VPS infrastructure.

### Secondary goals

-   Make the project strong enough to demonstrate Java Full Stack skills
    in interviews.
-   Follow production-style backend structure and coding practices.
-   Keep the system extensible for future features such as barcode
    scanning, suppliers, expenses, customers/udhaar, receipts, and
    expiry tracking.

## 3. Target Users

### Primary user: Shop Owner

The owner should be able to:

-   Add and manage products.
-   Check stock.
-   Add incoming stock.
-   Record sales.
-   View sales history.
-   Identify low-stock products.
-   View basic revenue/profit information.

### Future user: Shop Staff

A staff account may later be allowed to:

-   Search products.
-   Record sales.
-   View inventory.

Staff permissions should be more restricted than an admin/owner account.

## 4. Core Features

### 4.1 Authentication

Initial version:

-   Login/logout.
-   Secure password storage.
-   Role-based access structure.
-   Owner/Admin role.

Future:

-   Staff role.
-   Password reset.
-   Session/token management improvements.

### 4.2 Dashboard

The dashboard should show:

-   Today's sales.
-   Today's revenue.
-   Today's estimated profit.
-   Total products.
-   Total inventory quantity/value.
-   Low-stock product count.
-   Out-of-stock product count.
-   Recent sales.

The dashboard should remain simple and fast.

### 4.3 Product Management

A product should support at least:

-   ID.
-   Product name.
-   Category.
-   Brand (optional).
-   SKU/code (optional initially).
-   Unit.
-   Purchase price.
-   Selling price.
-   Current quantity.
-   Minimum stock level.
-   Active/inactive status.
-   Created timestamp.
-   Updated timestamp.

Supported units may include:

-   Piece.
-   Packet.
-   Box.
-   Bottle.
-   Kg.
-   Gram.
-   Litre.
-   Ml.

Product operations:

-   Create.
-   Read/list.
-   Update.
-   Deactivate/delete according to business rules.
-   Search.
-   Filter by category.
-   Filter by stock status.
-   Pagination for large lists.

### 4.4 Inventory Management

Inventory must support:

-   Stock addition.
-   Stock reduction through sales.
-   Manual stock adjustment.
-   Current stock display.
-   Minimum stock threshold.
-   Low-stock status.
-   Out-of-stock status.
-   Inventory/stock movement history.

Every meaningful stock change should be traceable.

Example:

``` text
Tata Salt
Previous stock: 20
Added: 10
New stock: 30
Reason: Purchase
```

### 4.5 Sales

The owner/staff should be able to:

-   Search/select products.
-   Enter quantities.
-   Add multiple products to a sale.
-   See line totals.
-   See the final total.
-   Complete the sale.
-   Automatically decrease inventory.
-   View the completed sale later.

The system must prevent a normal sale from reducing stock below
available quantity.

### 4.6 Sales History

The system should provide:

-   Sale date/time.
-   Sale ID.
-   Items.
-   Quantities.
-   Total amount.
-   Basic profit calculation.
-   Search/filter by date.

### 4.7 Reports

Initial reports:

-   Today's sales.
-   Daily sales.
-   Monthly sales.
-   Total revenue.
-   Estimated gross profit.
-   Best-selling products.
-   Low-stock products.

Reports should be simple and useful rather than overloaded with charts.

## 5. Business Rules

-   Selling price should not be negative.
-   Purchase price should not be negative.
-   Stock quantity cannot normally become negative.
-   A sale cannot contain zero or negative quantity.
-   A sale cannot exceed available stock.
-   Product names should be required.
-   Category should be required unless intentionally made optional by
    the final implementation.
-   Minimum stock should not be negative.
-   Stock changes must be recorded.
-   Completed sales should not silently disappear.
-   Product deletion should preferably be implemented as deactivation
    when historical sales depend on the product.
-   Money calculations must use a suitable decimal type such as Java
    `BigDecimal`, not floating-point arithmetic.
-   Sale creation and stock deduction must be handled transactionally.

## 6. Non-Functional Requirements

### Usability

-   Simple UI.
-   Mobile-friendly.
-   Fast product search.
-   Minimal unnecessary clicks.
-   Clear stock status indicators.
-   Clear error messages.

### Security

-   Passwords must never be stored as plain text.
-   Backend authorization must be enforced server-side.
-   Input must be validated.
-   Sensitive configuration must not be committed to Git.
-   Database credentials must be stored using environment
    variables/secrets.

### Reliability

-   Sales must not partially complete.
-   Stock and sale records must remain consistent.
-   Errors should be logged appropriately.
-   API errors should return predictable responses.

### Performance

The initial system targets a small shop and should comfortably handle
thousands of products and sales records with pagination and indexed
queries where appropriate.

## 7. Out of Scope for Version 1

Do not implement these unless a later phase explicitly requires them:

-   Online payments.
-   Customer mobile application.
-   Multi-store management.
-   Advanced accounting.
-   GST/tax compliance automation.
-   Complex supplier ERP.
-   AI recommendations.
-   Barcode hardware integrations.
-   Offline synchronization.
-   Advanced forecasting.

## 8. Future Features

Potential future additions:

-   Barcode scanning.
-   Barcode-based billing.
-   Printable/PDF receipts.
-   WhatsApp receipt sharing.
-   Supplier management.
-   Purchase orders.
-   Customer management.
-   Udhaar/credit tracking.
-   Expense tracking.
-   Expiry-date tracking.
-   Notifications.
-   Multiple staff accounts.
-   Role/permission management.
-   Cloud backups.
-   PWA/mobile installation.
-   Multi-shop support.

## 9. Success Criteria

Version 1 is successful when the shop owner can:

1.  Log in.
2.  Add products.
3.  Check current stock.
4.  Add incoming stock.
5.  Record a sale.
6.  See stock automatically decrease.
7.  Find low-stock products.
8.  View sales history.
9.  See basic daily/monthly sales information.
10. Use the application comfortably from a phone or laptop.

The application should be deployable and usable by the real shop owner
before additional advanced features are attempted.
