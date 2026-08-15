# ShopManager

A full-stack web application for small shop owners to manage their business — products, stock, sales, and reports — with per-owner data isolation. Built for any retail category: groceries, clothing, electronics, general stores, and more.

**Backend:** Java 21, Spring Boot 4.1, Spring Security (JWT), Spring Data JPA, PostgreSQL (Supabase)
**Frontend:** React 19, Vite, React Router, plain CSS

## Features

- Open registration — every user is a shop owner; no roles to configure
- Per-owner data isolation: each owner sees only their own categories, products, stock history, sales, and reports
- Cookie-based JWT sessions: HttpOnly + SameSite cookies, CSRF-protected, BCrypt-hashed passwords, stateless API
- Brute-force protection: rate limiting (429) on login, registration, and password change
- Product management with categories, units, cost/selling price, and low-stock thresholds
- Inventory tracking: stock-in, adjustments, automatic stock status (in stock / low / out)
- Sales: multi-item checkout with stock deduction, per-sale totals and profit
- Live dashboard: today's revenue, sale count, low-stock alerts, recent sales
- Reports: date-range summaries (revenue, profit, sales, per-category breakdown)
- Account settings: change password

## Quick Start

Prerequisites: Java 21, Node.js 20+, and a PostgreSQL database (project is configured for Supabase).

1. Configure the database — create `backend/.env`:
   ```
   DB_URL=jdbc:postgresql://<host>:5432/postgres
   DB_USERNAME=<user>
   DB_PASSWORD=<password>
   JWT_SECRET=<any long random string>
   ```
2. Start the backend (port 8081):
   ```
   cd backend
   .\mvnw.cmd spring-boot:run
   ```
3. Start the frontend (port 5173):
   ```
   cd frontend
   npm install
   npm run dev
   ```
4. Open http://localhost:5173 and register an account.

Hibernate auto-creates tables on startup (`ddl-auto=update`) — no manual SQL needed.

## Project Structure

```
backend/    Spring Boot REST API (controllers, services, repositories, entities, JWT security)
frontend/   React SPA (pages, API service layer, routing, auth guard)
```

## API Overview

Base URL: `http://localhost:8081/api`.

Auth is cookie-based: login sets an `HttpOnly` JWT cookie; state-changing requests must send the `X-XSRF-TOKEN` header (seeded from `GET /auth/csrf`). No token is stored in `localStorage`.

| Area | Endpoints |
|---|---|
| Auth (public) | `GET /auth/csrf`, `POST /auth/register`, `POST /auth/login`, `POST /auth/logout` |
| Auth (logged in) | `GET /auth/me` |
| Dashboard | `GET /dashboard/summary` |
| Categories | `GET/POST /categories` |
| Products | `GET/POST /products`, `GET/PUT/DELETE /products/{id}` |
| Inventory | `POST /inventory/stock-in`, `POST /inventory/adjustment`, `GET /inventory/movements` |
| Sales | `GET/POST /sales`, `GET /sales/{id}` |
| Reports | `GET /reports/summary?from=&to=` |
| Settings | `POST /settings/password` |

Errors are returned as consistent JSON: `{ status, message, timestamp }` — e.g. 400 (validation / insufficient stock), 401 (bad credentials), 403 (CSRF), 404 (missing resource), 409 (duplicate), 429 (rate limited).

## How Multi-Tenant Isolation Works

Every table carries an `owner_id`. A JWT filter identifies the caller from their session cookie, services scope every query by the current owner, and cross-owner lookups return 404 — so one shared database safely serves many independent shops.

## Security Notes

- Cookies are `HttpOnly` + `SameSite=Lax`; set `COOKIE_SECURE=true` in `backend/.env` when deploying behind HTTPS so the `Secure` flag is added.
- Set a strong `JWT_SECRET` in `backend/.env` — the fallback is a dev-only default.
- Rate limiting is in-memory (5 attempts / 15 minutes per user and IP for login, per IP for registration, per user for password changes) — swap in a Redis-backed limiter before running multiple server instances.
- The API also accepts `Authorization: Bearer <token>` as a fallback for external API clients.

## Testing

46 backend tests (services, controllers, security, rate limiting) run on an in-memory H2 database — no live database needed:

```
cd backend
.\mvnw.cmd test
```

Frontend: `npm run build` and `npm run lint` in `frontend/`.

## License

Private project — not open-sourced.
