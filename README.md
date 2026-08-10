# Grocery Manager — Grocery Shop Management System

A web-based **Java Full Stack** application to help a small grocery shop
manage products, inventory, sales, and basic business reports.

-   **Backend:** Java 21 · Spring Boot · Spring Web · Spring Data JPA ·
    Hibernate · PostgreSQL · Maven
-   **Frontend:** React (JavaScript) · Vite · CSS
-   Full product documentation lives in
    [`Grocery-Shop-Management-System-Docs/`](Grocery-Shop-Management-System-Docs/README.md).

## Project structure

```
├── Grocery-Shop-Management-System-Docs/   # PRD, Architecture, Rules, Phases, Design
├── backend/                               # Spring Boot REST API
│   └── src/main/java/com/grocery/manager/
└── frontend/                              # React single-page app
    └── src/
```

Progress and current next-step are tracked in
[`Grocery-Shop-Management-System-Docs/Memory.md`](Grocery-Shop-Management-System-Docs/Memory.md).

## Prerequisites

-   Java 21 (Temurin recommended)
-   PostgreSQL access — hosted on **Supabase** (see
    [Database setup](#database-setup)); no local Postgres needed
-   Node.js 20+ and npm

Maven is **not** required — the project uses the Maven Wrapper
(`./mvnw`), which downloads Maven automatically.

## Running the backend

```bash
cd backend
./mvnw spring-boot:run        # starts on http://localhost:8080
```

To build the jar:

```bash
./mvnw package
```

## Running the frontend

```bash
cd frontend
npm install
npm run dev                   # starts on http://localhost:5173
```

The Vite dev server proxies `/api/*` requests to the backend on
`http://localhost:8080`, so the two communicate without extra CORS
configuration during development.

## Database setup

PostgreSQL is hosted on **Supabase**, used strictly as the Postgres host
— no Supabase API features. The Spring Boot app connects with a plain
JDBC string.

The `dev` profile connects to the shared project database:

-   Host: `db.skzvyqtdufbemaqywxjo.supabase.co`, port `5432`
-   Database: `postgres`, user: `postgres`
-   Password: **you** provide it as the `DB_PASSWORD` environment
    variable. It is never stored in the repo.

```bash
cd backend
DB_PASSWORD="your-database-password" ./mvnw spring-boot:run
```

You can override the whole connection with `DB_URL`, `DB_USERNAME`, and
`DB_PASSWORD` if you prefer a different Postgres (for example a local
one) — see [`.env.example`](.env.example) for the full variable list.

`application-prod.properties` deliberately contains **no** defaults and
requires all values from the environment.

## Configuration profiles

| Profile | Purpose                                    |
| ------- | ------------------------------------------ |
| `dev`   | Default local development, safe defaults   |
| `prod`  | Production, all secrets from environment   |

Active profile is selected with `SPRING_PROFILES_ACTIVE=prod` (the
default is `dev`).

## API

All endpoints use the `/api` prefix. See
[`Grocery-Shop-Management-System-Docs/Architecture.md`](Grocery-Shop-Management-System-Docs/Architecture.md)
for the endpoint map. Authentication and the feature endpoints are added
in later phases.