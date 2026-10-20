# BrightCore Recruitment

A small recruitment web app. The public can browse open jobs and apply with a CV, and an administrator posts jobs and reviews applications.

- **Frontend:** Next.js 15 (App Router, TypeScript, Tailwind CSS)
- **Backend:** Spring Boot 3.5 (Java 21), Spring Security, Spring Data JPA, Flyway
- **Database:** PostgreSQL 16

---

## Quick start (one command)

Requires Docker.

```bash
docker compose up --build
```

Open **http://localhost:3000**. The admin area is at **http://localhost:3000/admin**.

| Seeded admin login |                       |
| ------------------ | --------------------- |
| Email              | `admin@brightcore.rw` |
| Password           | `Admin@12345`         |

On first start the app seeds this admin account and four sample jobs: three open and one closed. The closing dates are set relative to the current date, so the open jobs are still open whenever you run the app. Data and uploaded CVs are kept in Docker volumes and survive restarts. Run `docker compose down -v` to reset everything.

## Running without Docker

Prerequisites: Java 21, Maven, Node 22+ and a PostgreSQL database.

```bash
# 1. Database (any PostgreSQL 14+)
createuser recruitment --pwprompt   # password: recruitment
createdb -O recruitment recruitment

# 2. Backend → http://localhost:8080
cd backend
mvn spring-boot:run

# 3. Frontend → http://localhost:3000 (in a second terminal)
cd frontend
npm install
npm run dev
```

The backend reads its settings from environment variables, with defaults for local development:

| Variable                                  | Default                                         | Purpose                          |
| ----------------------------------------- | ----------------------------------------------- | -------------------------------- |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD`  | `jdbc:postgresql://localhost:5432/recruitment`, `recruitment`, `recruitment` | Database connection |
| `JWT_SECRET`                              | dev-only value                                  | Signs admin tokens (32+ chars). **Set in production.** |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD`          | `admin@brightcore.rw` / `Admin@12345`           | Seeded admin account             |
| `UPLOADS_DIR`                             | `./uploads`                                     | Where CVs are stored             |
| `COOKIE_SECURE`                           | `false`                                         | Set `true` when served over HTTPS |
| `APP_TIMEZONE`                            | `Africa/Kigali`                                 | Defines "today" for closing dates |

The frontend needs `BACKEND_URL` (default `http://localhost:8080`).

## Tests

```bash
cd backend && mvn test        # 40 tests: unit + integration (in-memory H2, no setup needed)
cd frontend && npm test       # validation and formatting unit tests
```

The backend integration tests start the full Spring context and call the real HTTP endpoints through MockMvc. They cover:

- applying successfully, with the application stored as **Pending** and the email and phone normalised
- rejecting duplicate applications for the same email, ignoring case
- refusing applications for closed jobs and for jobs past their closing date (the closing date itself is inclusive)
- field validation errors, a missing CV, a non-PDF file and a file that has a `.pdf` name but isn't a PDF
- accepting and rejecting with an optional note, and blocking a move back to Pending
- filtering applicants by status, and the overview counts
- the auth wall on every admin endpoint, login, the HttpOnly cookie, tampered tokens and the lockout after repeated failures

GitHub Actions ([.github/workflows/ci.yml](.github/workflows/ci.yml)) runs the backend tests, the frontend type check and tests, and a production build on every push.

---

## Features

**Public site**
- Jobs page listing open jobs (title, location, type, closing date, summary), with search, a type filter and pagination
- Job details page with the full description, responsibilities and requirements
- Application form (name, email, phone, cover note, PDF CV up to 5 MB), with inline validation and a confirmation message
- Closed or expired jobs hide the form. The API also refuses applications to them.
- The same email can't apply twice to the same job

**Admin area**
- Login with protected pages and API endpoints
- Create, edit, close and reopen jobs
- Applicants per job, with a filter by status, an application detail page and a CV download
- Accept or reject with an optional note. Every application starts as Pending.
- Overview with application counts per job and per status
- Mock email to the applicant when their status changes (written to the backend log)

## Technology choices

| Choice | Why |
| ------ | --- |
| **Spring Boot** | Mature, well-documented, and matches BrightCore's stack. Spring Security, Bean Validation and JPA cover most of the brief's security and validation needs out of the box. |
| **Next.js (App Router)** | Public pages are rendered on the server, so they're fast and search engines can index them. Admin pages are client components because they're interactive and private. |
| **PostgreSQL + Flyway** | A real relational database with constraints, such as the unique `(job_id, email)` that blocks duplicate applications. Flyway gives a versioned, repeatable schema. |
| **JWT in an HttpOnly cookie** | Stateless auth that page JavaScript can't read, so an XSS bug can't steal it. Storing the token in `localStorage` would allow that. |
| **Next.js `/api` proxy** | The browser talks only to the Next.js origin, so CORS isn't needed and the cookie can be `SameSite=Strict`. |
| **H2 for tests** | The tests run anywhere with no database to install. Flyway runs the same migrations against it. |

## Project structure

```
backend/src/main/java/rw/brightcore/recruitment/
  job/            Job entity, public job endpoints, filters (JPA Specifications)
  application/    Applying, CV storage and validation, status changes
  admin/          Admin endpoints (jobs, applicants, overview)
  auth/           Login, JWT, cookie, auth filter, lockout
  notification/   Mock email sent after a status change commits
  config/         Security, typed settings, clock, seed data
  common/         Error handling and shared response types
backend/src/main/resources/db/migration/   Flyway SQL migrations

frontend/src/
  app/(public)/   Jobs list, job details, application form
  app/admin/      Login and the protected admin pages
  components/     Shared UI (form fields, badges, job form…)
  lib/            API client, types, validation, formatting
  middleware.ts   Redirects to login when there is no auth cookie
```

## API overview

| Method | Path | Access |
| ------ | ---- | ------ |
| GET | `/api/jobs?search=&type=&page=&size=` | Public: open jobs only |
| GET | `/api/jobs/{id}` | Public |
| POST | `/api/jobs/{id}/applications` (multipart) | Public |
| POST | `/api/auth/login`, `/api/auth/logout` | Public |
| GET | `/api/auth/me` | Admin |
| GET / POST | `/api/admin/jobs` | Admin |
| GET / PUT | `/api/admin/jobs/{id}` | Admin |
| POST | `/api/admin/jobs/{id}/close`, `/reopen` | Admin |
| GET | `/api/admin/jobs/{id}/applications?status=` | Admin |
| GET | `/api/admin/applications/{id}` | Admin |
| PATCH | `/api/admin/applications/{id}/status` | Admin |
| GET | `/api/admin/applications/{id}/cv` | Admin |
| GET | `/api/admin/overview` | Admin |

Errors always use the same shape: `{ "status": 400, "message": "...", "errors": { "field": "message" } }`.

## Security and data handling

- **Passwords** are hashed with BCrypt and the plain password is never stored. Login responses take the same time whether or not the email exists, and an email is locked for 15 minutes after 5 failed attempts.
- **Authorisation:** Spring Security denies by default. Only the listed public routes are open, and everything under `/api/admin/**` requires the admin role. The Next.js middleware only improves the experience (an early redirect); the API is the real gatekeeper.
- **Injection:** all queries go through JPA with bound parameters, including search, which uses JPA Specifications. LIKE wildcards in user input are escaped.
- **Validation** runs on the server for every field with Bean Validation, plus business rules in the services. The client-side checks are only for faster feedback.
- **CV uploads** are checked for size (5 MB or less), the `.pdf` extension, the content type, and the `%PDF-` signature in the file itself, because the name and type are easy to fake. Files are saved under random UUID names, outside any public folder. They can only be downloaded through an admin-only endpoint, as `attachment`, with `nosniff` and `no-store` headers.
- **Duplicate applications** are checked in the service for a clear message, and also enforced by a database unique constraint in case two submissions arrive at the same moment.
- **No internals leak:** stack traces and server details are never returned to the client.

## Assumptions

- A single admin role is enough. The seeded account comes from environment variables.
- A job accepts applications while it's **Open** and today, in the `Africa/Kigali` time zone, is **on or before** the closing date.
- Closed jobs keep their public detail page so old links still work, but the page says applications are closed and the API refuses them.
- An application starts as **Pending**. An admin can change it to **Accepted** or **Rejected**, and can change that decision later to fix a mistake, but can't set it back to Pending.
- Emails are compared without regard to case, so `Aline@x.com` and `aline@x.com` count as the same person.
- When a job's closing date is edited, the new date can't be in the past. Other edits to old jobs are still allowed.
- Status emails are mocked: they're written to the backend log, as the brief allows.

## What I would improve with more time

- **Real email delivery** (SMTP or a provider) through an outbox table with retries.
- **Object storage** (S3-compatible) for CVs instead of local disk, plus virus scanning of uploads.
- **Pagination and search** on the admin applicants list, plus CSV export.
- **More tests:** end-to-end browser tests (Playwright) and repository tests against real PostgreSQL with Testcontainers.
- **Audit log** of who changed which status and when, and support for several admin users and roles.
- **Rate limiting** on the public application endpoint, and a shared store (Redis) for login lockouts so they work across several instances.
- **Observability:** structured logs, metrics and alerting, plus a deployed staging environment.
