# Grocery Manager — Memory / Project Progress

> Working memory. Records actual progress, decisions, known issues, and
> the current next step. Updated as implementation happens.

## Status: Phase 3 — Sales (complete ✅)

## Phase 3 — Sales (completed)

### Goal reached

A user can complete a sale and the correct inventory reduction happens
atomically.

### Backend (`com.grocery.manager`)

-   Entities `Sale` (items cascade, `totalAmount`, `createdAt`) and
    `SaleItem` (product, quantity, **price snapshots**: `unitPrice`
    charged + `purchasePrice` for profit). Prices are copied at sale
    time so later product price changes do not rewrite history.
-   `MovementType` gained `SALE`; a sale records one stock movement per
    product (`-quantity`, reason `Sale #<id>`).
-   `SaleService.createSale` is a single `@Transactional` unit:
    1.  Reject an empty item list.
    2.  Load products once (map keyed by id) and accumulate quantities
        across duplicate line items.
    3.  **Validate available stock** cumulatively — a sale cannot exceed
        what is in stock (new `InsufficientStockException` → 409
        `INSUFFICIENT_STOCK` with product name + available/required).
    4.  Snapshot prices, compute line totals and the sale total
        (BigDecimal, `HALF_UP` to 2dp).
    5.  Save the sale (cascade items), reduce each product's
        `currentQuantity`, write SALE movements.
-   Atomicity verified: a failed insert left **no** partial sale and
    **no** stock change (whole tx rolled back).
-   Endpoints: `POST /api/sales` (201), `GET /api/sales` (paged
    `SaleSummaryResponse`), `GET /api/sales/{id}` (full `SaleResponse`
    with items). Sortable by id/totalAmount/createdAt, newest first.
-   `GlobalExceptionHandler` gained handlers for
    `InsufficientStockException` (409) and `IllegalArgumentException`
    (400).
-   Tests: `SaleServiceTest` 8 cases → full suite now **27 tests pass**.

### Migration note (IMPORTANT)

Adding `MovementType.SALE` did **not** change the existing
`stock_movements` check constraint because Hibernate `ddl-auto=update`
does not migrate already-created enum check constraints. The constraint
`stock_movements_type_check` was updated **manually** on the hosted DB
(one-time SQL) to include `'SALE'`. Any future enum change needs the
same manual step unless Flyway/Liquibase is adopted.

### Frontend

-   `services/sales.js` (`listSales`, `getSale`, `createSale`).
-   `SalesPage` — paginated sale history (sale #, date, item count,
    total).
-   `NewSalePage` — product search (debounced) → add to the "bag" with
    quantity edits, line totals, running total, **Complete Sale**
    (duplicate lines merge client-side); insufficient stock shows the
    server's 409 message.
-   `SaleDetailPage` — items table + estimated gross profit (from the
    `purchasePrice` snapshot).
-   Routes: `/sales`, `/sales/new`, `/sales/:id`.

### Verified end-to-end (hosted Postgres)

-   Stock-in `Full Cream Milk +10` → 201 (8 → 18).
-   Sale of `3 × Full Cream Milk` (₹26) → 201, `totalAmount 78.00`,
    items carry `unitPrice 26.00` + `purchasePrice 20.00`.
-   Product stock 18 → 15 after the sale; a `SALE` movement (`−3`, reason
    `Sale #2`) recorded.
-   Over-stock sale (`Oil 999`) → 409 `INSUFFICIENT_STOCK` with a clear
    message.
-   `GET /api/sales`, `GET /api/sales/2` correct; all `/api` proxy
    checks through the Vite dev server return 200; `npm run build` clean.

Demo data on the hosted DB (sale #2, movements) may be cleaned via the
app.

## Phase 2 — Completed

### Goal reached

Stock changes are tracked accurately and product low/out-of-stock status
is derived from quantity vs. minimum stock level.

### Backend (`com.grocery.manager`)

-   Entity `StockMovement`: product (ManyToOne), `MovementType` enum
    (`STOCK_IN`, `ADJUSTMENT`), `previousQuantity` /
    `quantityChanged` (signed) / `newQuantity` (BigDecimal 12,3), reason
    (max 200), `createdAt`. Indexed on `product_id` and `created_at`.
-   `StockMovementRepository`: paged `findByProductId`.
-   `InventoryService` (@Transactional): `stockIn` (adds stock, records
    prev → new), `adjust` (corrects stock to an absolute value, delta
    signed), `listMovements(productId?, pageable)`.
-   `InventoryController` under `/api/inventory`:
    -   `POST /api/inventory/stock-in` — stock-in (quantity must be > 0;
        bean-validated 400 otherwise).
    -   `POST /api/inventory/adjustment` — set stock to a new value
        (must be ≥ 0 → negative stock is impossible).
    -   `GET /api/inventory/movements` — paginated history, optional
        `productId` filter, default sort newest-first (`createdAt desc,
        id desc`).
-   Missing product → 404 (`PRODUCT_NOT_FOUND`), validation → 400 with
    fieldErrors. Consistent with existing `ApiError` handling.
-   Tests: 6 new `InventoryServiceTest` cases (stock-in, adjustment,
    deltas, not-found, list + filter). Full suite: **19 tests pass**.

### Frontend

-   `InventoryPage` replaces the placeholder at `/inventory`:
    -   Segmented control toggling **Stock In** / **Adjust Stock** forms
        (product picker, quantity, optional reason; shows current stock
        of the selected product).
    -   **Movement history** table (date/time, product+SKU, type badge,
        previous → new stock, signed color-coded change, reason),
        product filter, pagination.
    -   Success/error alerts; form resets after a successful operation.
-   `services/inventory.js` (`stockIn`, `adjustStock`,
    `listMovements`); `MOVEMENT_TYPE_LABELS`; `formatDateTime` util.
-   Verified: `npm run build` clean; history loads through the Vite proxy.

### Verified end-to-end (hosted Postgres)

-   Stock-in `Oil +20` → 201 (prev 0 → new 20).
-   Adjustment `Oil → 8` → 201 (change −12) → `stockStatus` now
    `LOW_STOCK` (8 ≤ min 10).
-   History returns both movements newest-first; `?productId=3` filters.
-   quantity 0 → 400; missing product → 404 clean JSON.
-   `/api/inventory/movements` through Vite proxy → HTTP 200.

Demo data left from verification: product **Oil** now has 8 in stock
(LOW_STOCK) and two stock movements. Can be cleaned/edited through the
app.

## Phase 1 — Completed

Backend (`com.grocery.manager`):

-   Entities: `Category`, `Product`, enums `Unit`, `StockStatus`, with
    fields per PRD (name, category, brand, sku, unit, purchase/selling
    price, current quantity, minimum stock, active, timestamps). Money
    and quantities are `BigDecimal`; indexes on name/category/sku.
-   Repositories: `ProductRepository` (JpaSpecificationExecutor) +
    `CategoryRepository` (unique-name checks).
-   Services: `ProductService` (create/update/list/get/deactivate) +
    `CategoryService`. `Product` list supports `search`, `categoryId`,
    `stockStatus` filters and pagination.
-   Controllers: `ProductController`, `CategoryController` under `/api`.
-   DTOs: `product/` and `category/` request/response records.
-   Centralised exception handling: `GlobalExceptionHandler` returns a
    consistent `ApiError` JSON (400 with fieldErrors, 409 duplicates,
    404 not found, 500 generic).
-   Deactivate = soft delete (`active=false`) so historical data is
    preserved. SKU uniqueness enforced when provided.
-   Tests: 13 unit tests pass (`ProductServiceTest`, `CategoryServiceTest`).
    CRUD + filters verified against hosted Postgres via curl.

Frontend:

-   React Router added; `Layout` sidebar with active nav states.
-   Products list page (search, category filter, stock-status filter,
    pagination, deactivate with confirm) and add/edit form page (with
    inline "add new category").
-   Services layer (`api.js` wrapper, `products.js`, `categories.js`),
    stock-status badges, INR currency formatting, unit labels.
-   Verified: `npm run build` clean; dev server 200; Vite `/api` proxy
    reaches the backend; product list renders against hosted Postgres.

Demo data left on the hosted DB from verification: category `Dairy`,
products `Full Cream Milk`, `Oil` (active) and `Salt` (deactivated).
They can be removed through the app.

## Phase 0 — Project Setup (completed)

### Completed

-   Git repository initialized (`main` branch) with root `.gitignore`
    covering secrets, `backend/target`, `frontend/node_modules`, IDE
    files.
-   Backend scaffolded from Spring Initializr into `backend/`:
    -   Spring Boot **4.1.0**, Java **21**, Maven Wrapper 3.9.16.
    -   Dependencies: `webmvc`, data JPA, validation, PostgreSQL.
    -   Builds successfully — executable jar produced.
-   Backend configuration:
    -   `application.properties` (base), `application-dev.properties`,
        `application-prod.properties`.
    -   `dev` profile: PostgreSQL (hosted on Supabase) with env-var
        overrides (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`); the
        password has **no default**; `ddl-auto=update`.
    -   `prod` profile: all secrets from environment, no defaults;
        `ddl-auto=validate`.
-   Backend verified end-to-end:
    -   Starts on `:8080` and **connects to the hosted PostgreSQL**
        (Supabase, PostgreSQL **17.6**, `PostgreSQLDialect`) using the
        `DB_PASSWORD` env var.
    -   HTTP responds on `:8080` (404 JSON error bodies until
        controllers exist).
    -   `./mvnw test` passes (`contextLoads`), proving the app context
        and DB connection work.
-   Frontend scaffolded with Vite (React 19, JavaScript) into
    `frontend/`:
    -   Design tokens applied from `Design.md` (green primary, neutral
        base, 4px spacing scale).
    -   Simple responsive app shell (sidebar + topbar) with placeholder
        nav items (Dashboard, Products, Inventory, Sales, Reports,
        Settings). No routing yet — planned for Phase 1.
    -   Vite dev server proxies `/api` → `http://localhost:8080`.
    -   Verified: `npm run build` succeeds; dev server returns HTTP 200.
-   Root `README.md` written with structure, run instructions, and
    database setup.

### Decisions

-   **Package name:** `com.grocery.manager` (docs asked to replace
    `com.example.grocery`).
-   **Database:** PostgreSQL, hosted on Supabase. The initial plan was a
    local install; this was **changed** to the ready Supabase-hosted
    Postgres (no local install).
-   **Supabase = PostgreSQL hosting only (NOT a backend).** Supabase's
    API/BaaS features are intentionally **not** used. All data work
    (entities, JPA repositories, queries, transactions) stays in Spring
    Boot. Spring Boot connects to the hosted Postgres with a plain JDBC
    connection string — nothing goes through Supabase's API layer.
    -   Hosted Postgres connection details:
        -   host: `db.skzvyqtdufbemaqywxjo.supabase.co`
        -   port: `5432` (direct connection; IPv4-only networks need
            the IPv4 add-on or the transaction pooler on `6543`)
        -   database: `postgres`, user: `postgres`
        -   password: provided via environment variable only, never
            committed.
    -   State: **decided** — the Supabase-hosted Postgres is the
        **development database now** (no local PostgreSQL install).
        The dev profile defaults `DB_URL`/`DB_USERNAME` to it; the
        password has no default and must be supplied via `DB_PASSWORD`.
        The real password was shared once in chat (user advised to
        rotate it), is never committed, and is provided as an env var.
-   **No Maven on PATH** → using Maven Wrapper; no additional install
    needed.
-   **Frontend JS (not TS)** for now, per docs and scope restraint.
-   **No Spring Security yet** — deferred to Phase 5 (auth).

### Known issues / open items

-   **DB password rotation:** the real Supabase password was typed into
    chat once; it is advised to **rotate it** in the Supabase dashboard.
-   **IPv4 caveat:** the direct pool on `:5432` may prefer IPv6. If the
    connection ever fails from an IPv4-only network, use the Supabase
    transaction pooler (port `6543`).
-   The user commits/pushes themselves (repo has no Claude-configured
    git identity). Phase 1–3 files are uncommitted; the user
    stages/commits when ready.

### Current next step

The user commits latest progress themselves (Phase 1–3 files are
uncommitted). Then begin **Phase 4 — Dashboard**:

-   `GET /api/dashboard/summary` returning: today's sales count + revenue
    + estimated profit, total products, total inventory quantity/value,
    low-stock count, out-of-stock count, recent sales.
-   Derived queries aggregate `sales`/`sale_items` (today's totals via
    date range on `created_at`), `products` (counts, qty vs minimum),
    and recent sales reuse `saleRepository`.
-   Frontend: replace the Dashboard placeholder with stat tiles + a
    recent-sales table (no charts — per Design docs keep it simple).
-   Existing demo data on the hosted DB may be cleaned via the app.

### Phase checklist (from `Phases.md`)

-   [x] Create Git repository
-   [x] Create Spring Boot backend
-   [x] Configure Maven (wrapper)
-   [x] Configure PostgreSQL (hosted on Supabase)
-   [x] Create React frontend
-   [x] Configure environment-specific settings
-   [x] Establish initial folder structure
-   [x] Create basic README
-   [x] Verify frontend starts
-   [x] Verify backend starts
-   [x] Verify backend connects to the database