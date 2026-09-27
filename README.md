# QR Code Generator

A full-stack QR Code Generator web application. Users register, log in, generate
QR codes (text, URL, email, phone, Wi-Fi, vCard/contact), view their history,
download codes as PNG, and manage their account — all served from a **single
port** (`http://localhost:8080`).

## Tech Stack

**Backend:** Java 17, Spring Boot 3, Spring Web, Spring Data JPA, Spring Security
(session-based auth, BCrypt, CSRF), MySQL 8, Maven, Lombok, ZXing (QR generation),
JUnit 5, Mockito, MockMvc.

**Frontend:** React 18, Vite, React Router, Axios, plain CSS.

**Deployment model:** Spring Boot serves the React production build as static
resources from the *same* origin/port — there is no separate frontend server or
port in the final build.

---

## 1. Requirements

- Java 17+
- Maven 3.8+
- Node.js 18+ / npm (only needed if you want to run the frontend dev server
  separately, or don't want Maven to download its own Node — see note below)
- MySQL 8+
- Git
- VS Code (recommended, see §8)

> **Note:** `backend/pom.xml` uses the `frontend-maven-plugin`, which downloads
> its own isolated Node.js/npm into `frontend/` during the Maven build — you do
> **not** need Node installed on your machine just to build/run the final app.
> Node is only required locally if you want to run `npm run dev` yourself for
> frontend-only development.

---

## 2. Database Setup

Create the database:

```bash
mysql -u root -p < setup.sql
```

or manually:

```sql
CREATE DATABASE qr_generator_db;
```

Tables (`users`, `qr_codes`) are created/updated automatically by Hibernate on
application startup (`spring.jpa.hibernate.ddl-auto=update`) — no manual DDL
needed.

---

## 3. Configuration

Database credentials are read from environment variables, with safe local
defaults defined in `backend/src/main/resources/application.properties`:

| Variable      | Default | Description            |
|---------------|---------|-------------------------|
| `DB_USERNAME` | `root`  | MySQL username          |
| `DB_PASSWORD` | *(placeholder)* | MySQL password |

Set them before running, e.g.:

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_actual_password
```

`application.properties.example` is provided as a template — copy values from
it rather than committing real credentials. `application.properties` itself
also ships with placeholder values and is safe to keep in the repo as long as
you override the password via environment variable in real use.

**Never commit real database passwords to Git.**

---

## 4. Running the Application (production-style, single port)

From the `backend/` directory:

```bash
cd backend
mvn clean package
```

This single command:
1. Installs an isolated Node.js/npm (via frontend-maven-plugin).
2. Installs frontend dependencies (`npm install`).
3. Builds the React app (`npm run build`) into `frontend/dist`.
4. Copies `frontend/dist` into the Spring Boot jar's static resources.
5. Packages everything into one runnable jar.

Then run it:

```bash
java -jar target/qr-generator.jar
```

Or build and run in one step:

```bash
mvn clean spring-boot:run
```

**Open the application at:**

```
http://localhost:8080
```

You do **not** need to start React separately — there is no port 5173 in the
production flow.

---

## 5. Development Workflow (optional, two terminals)

If you're actively developing the frontend and want hot reload, you can
temporarily run Vite's dev server, which proxies API calls to the backend
(see `frontend/vite.config.js`):

**Terminal 1 — backend:**
```bash
cd backend
mvn spring-boot:run
```

**Terminal 2 — frontend (dev only):**
```bash
cd frontend
npm install
npm run dev
```
Visit `http://localhost:5173` during development. This is for convenience
only — the final/shipped application always runs from `http://localhost:8080`
via `mvn clean package` as described in §4.

---

## 6. Running Tests

From `backend/`:

```bash
mvn test
```

This runs:
- `AuthServiceTest` — registration, duplicate email, password hashing, login
- `QRCodeServiceTest` — QR generation for all 6 types, history, delete, ownership checks
- `QRCodeGeneratorTest` — image generation, dimensions, invalid input
- `AuthControllerTest` — `/api/auth/register`, `/login`, `/me` via MockMvc
- `QRCodeControllerTest` — `/api/qrcodes` CRUD via MockMvc
- `SecurityRequirementsTest` — unauthenticated access denial, cross-user
  ownership isolation, BCrypt hashing verification

---

## 7. API Overview

| Method | Endpoint                     | Auth required | Description                     |
|--------|-------------------------------|:--:|----------------------------------|
| POST   | `/api/auth/register`          | No | Register a new account          |
| POST   | `/api/auth/login`             | No | Log in (session cookie set)     |
| POST   | `/api/auth/logout`            | Yes | Log out (session invalidated)  |
| GET    | `/api/auth/me`                | Yes | Current authenticated user      |
| GET    | `/api/users/profile`          | Yes | Get profile                     |
| PUT    | `/api/users/profile`          | Yes | Update full name                |
| PUT    | `/api/users/password`         | Yes | Change password                 |
| POST   | `/api/qrcodes`                | Yes | Generate a QR code              |
| GET    | `/api/qrcodes`                | Yes | List your QR code history       |
| GET    | `/api/qrcodes/{id}`           | Yes | Get one QR code (owner only)    |
| DELETE | `/api/qrcodes/{id}`           | Yes | Delete a QR code (owner only)   |
| GET    | `/api/qrcodes/{id}/download`  | Yes | Download QR code as PNG         |

Supported QR types (`type` field on `POST /api/qrcodes`): `TEXT`, `URL`,
`EMAIL`, `PHONE`, `WIFI`, `VCARD`.

---

## 8. VS Code Setup

Recommended extensions:
- Extension Pack for Java
- Spring Boot Extension Pack
- ESLint
- Prettier
- MySQL (by Jun Han, or similar)

Steps:
1. Open the repository root folder in VS Code.
2. Let the Java extension pack index `backend/` (it detects `pom.xml`
   automatically).
3. Configure your MySQL connection using the MySQL extension, or just use the
   `mysql` CLI as in §2.
4. `cd frontend && npm install` if you plan to run the Vite dev server.
5. Run backend tests: `cd backend && mvn test` (or use the Testing sidebar).
6. Build: `cd backend && mvn clean package`.
7. Start: `java -jar backend/target/qr-generator.jar` (or use the Spring Boot
   Dashboard extension's "Run" button).
8. Open `http://localhost:8080` in your browser.


---

## 10. Security Notes

- Passwords are hashed with **BCrypt** and never stored or returned in plain text.
- Authentication is **session-based** (Spring Security), with the session
  cookie set on login and invalidated on logout.
- **CSRF protection** is enabled via a cookie-based token
  (`CookieCsrfTokenRepository`); Axios is configured to read the `XSRF-TOKEN`
  cookie and automatically send it back as the `X-XSRF-TOKEN` header on
  state-changing requests.
- Every QR code endpoint verifies **ownership** server-side — a user can never
  read, download, or delete another user's QR codes, regardless of what ID is
  requested.
- QR image dimensions requested by the frontend are validated against a fixed
  allow-list (`200`, `300`, `500`) on the backend — arbitrary client-supplied
  sizes are rejected.
- All error responses are sanitized (`GlobalExceptionHandler`) — no stack
  traces are ever returned to the client.

---