# Grocery Manager — Memory / Project Progress

> Working memory. Records actual progress, decisions, known issues, and
> the current next step. Updated as implementation happens.

## Status: Phase 0 — Project Setup (complete ✅)

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
-   Git has **no configured identity** (neither global nor repo-local);
    committing is left to the user.

### Current next step

Commit the staged Phase 0 work, then begin **Phase 1 — Product
Management**:

-   Backend: `Category` + `Product` entities, repositories, service,
    controller, DTOs, validation, centralized exception handling.
-   API: `GET/POST /api/products`, `GET/PUT/DELETE /api/products/{id}`,
    `GET/POST /api/categories`.
-   Frontend: product list, add/edit forms, deactivate, search, filter.
-   `ddl-auto=update` (dev) will create the tables on the hosted
    Postgres automatically.

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