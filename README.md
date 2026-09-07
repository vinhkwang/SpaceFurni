# SpaceFurni

A furniture e-commerce platform: a public storefront, an admin console, and the API behind both.

## Stack

| App | Stack | Port |
|---|---|---|
| `client/` | Next.js 16, React 19, Tailwind v4, TypeScript | 3000 |
| `admin/` | Next.js 16, React 19, Tailwind v4, TypeScript | 3001 |
| `server/` | Spring Boot 4.1, Java 21, Maven, PostgreSQL | 8080 |

## Setup

1. Copy the env files and adjust as needed:
   ```bash
   cp .env.example .env
   cp server/.env.example server/.env
   cp admin/.env.example admin/.env.local
   ```
2. Start Postgres and create the database:
   ```bash
   sudo systemctl start postgresql
   sudo -u postgres psql -c "DROP DATABASE IF EXISTS spacefurni WITH (FORCE);"
   sudo -u postgres psql -c "CREATE DATABASE spacefurni OWNER spacefurni;"
   ```
3. Run each app in its own terminal:
   ```bash
   cd server && ./mvnw spring-boot:run
   ```
   ```bash
   cd client && npm run dev
   ```
   ```bash
   cd admin && npm run dev
   ```

## Seed data

Seed data runs automatically the first time the server starts against an empty database — no separate command needed. To re-seed, reset the database (step 2 above) and start the server again.

Seeded accounts:

| Role | Email | Password |
|---|---|---|
| Admin | `admin@spacefurni.dev` | `DevAdmin123!` |
| Customer | `customer@spacefurni.dev` | `DevCustomer123!` |

## Useful commands

```bash
cd server && ./mvnw clean verify
cd client && npx tsc --noEmit && npm run lint && npm run build
cd admin && npx tsc --noEmit && npm run lint && npm run build
```

