# Task Manager REST API — Spring Boot 3 + JWT + MySQL

A clean, production-style **REST API** built to demonstrate **backend Java skills**:
Spring Boot 3, Spring Security with **JWT authentication**, Spring Data JPA, and a
relational database (**MySQL**, with a ready-made **SQL Server** profile).

Every user registers, logs in to receive a **JWT**, and then manages **their own tasks**
(owner-based authorization). Passwords are hashed with **BCrypt**; tokens are signed
**HS256** with a configurable secret and expiry.

---

## ✨ Features

- ✅ JWT authentication (register → login → **Authorization: Bearer <token>**)
- 🔐 Stateless security (no sessions) — a real-world, scalable API design
- 👤 User ownership: every user only sees / edits / deletes **their own** tasks
- 🧱 Layered architecture: `controller → service (DTO) → repository → entity`
- 🧾 Validated request bodies (Jakarta Validation) + clean JSON error responses
- 🔑 BCrypt password hashing, role claim inside the token (`ROLE_USER`)
- 🗄️ Database-agnostic: MySQL (default) and SQL Server (profile) — both included
- 🧪 Tests run on **H2 in-memory** (no DB server needed to run `mvn test`)

---

## 🛠️ Tech Stack

| Layer          | Technology                                            |
|----------------|-------------------------------------------------------|
| Language       | Java 17                                               |
| Framework      | Spring Boot 3.3.5                                     |
| Security       | Spring Security 6 + JWT (jjwt 0.12.6, HS256)          |
| Persistence    | Spring Data JPA / Hibernate                           |
| Databases      | MySQL 8 (default), SQL Server (profile), H2 (tests)   |
| Build          | Maven 3.9+                                            |
| Extras         | Lombok, Jakarta Validation, REST with DTOs            |

---

## 🔄 How JWT Authentication Works

```
1. POST /api/auth/register   →  saves user (password hashed with BCrypt)
2. POST /api/auth/login      →  validates credentials, returns a signed JWT
3. Client sends every request with:   Authorization: Bearer <token>
4. JwtAuthenticationFilter   →  validates token signature + expiry
5. Valid token → user loaded into SecurityContext → request authorized
6. Expired / invalid token  →  401 Unauthorized
```

Token structure (HS256): `header.payload.signature`

| Claim       | Meaning                          |
|-------------|----------------------------------|
| `sub`       | Email (login username)           |
| `roles`     | `ROLE_USER` (ready for RBAC)     |
| `iat`       | Issued-at timestamp              |
| `exp`       | Expiry (24h by default)          |

---

## 📁 Project Structure

```
task-manager-jwt-api/
├── pom.xml
└── src/
    ├── main/java/com/example/taskapi/
    │   ├── TaskApiApplication.java
    │   ├── config/          # SecurityConfig, ApplicationConfig (beans)
    │   ├── security/        # JwtService, JwtAuthenticationFilter
    │   ├── user/            # User entity, Role, UserRepository
    │   ├── auth/            # AuthController, AuthService, DTOs
    │   ├── task/            # Task entity, TaskController, TaskDto, TaskRepository
    │   └── exception/       # GlobalExceptionHandler, ApiError, NotFoundException
    ├── main/resources/      # application.properties (MySQL) + sqlserver profile
    └── test/                # Context + JWT unit tests (H2)
```

---

## 🚀 Quick Start (MySQL)

### 1. Prerequisites
- **Java 17+** and **Maven 3.9+**
- A running **MySQL 8** instance (local or Docker)

### 2. Configure the database
Edit `src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

The database `taskdb` is **created automatically** (`createDatabaseIfNotExist=true`).

💡 *No MySQL yet? Run it with Docker:*
```bash
docker run --name mysql -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:8
```

### 3. Run the application
```bash
mvn spring-boot:run
```
Server starts on **http://localhost:8080**.

### 4. Try it out
```bash
# Register a user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Ahmed Hassan","email":"ahmed@example.com","password":"secret123"}'

# Login → receive the JWT
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ahmed@example.com","password":"secret123"}'

# Use the token (paste your real token)
curl http://localhost:8080/api/tasks \
  -H "Authorization: Bearer <YOUR_TOKEN>"

# Create a task
curl -X POST http://localhost:8080/api/tasks \
  -H "Authorization: Bearer <YOUR_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"title":"Prepare for the technical interview","description":"Review OOP, SQL and JWT","completed":false}'
```

---

## 🗄️ Using SQL Server instead

The job at hand also mentions SQL Server — a profile is included:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=sqlserver
```

Edit `src/main/resources/application-sqlserver.properties` and adjust
`host / port / database / sa password` for your instance.

---

## 📡 API Reference

| Method | Endpoint              | Auth  | Description                          |
|--------|-----------------------|-------|--------------------------------------|
| POST   | `/api/auth/register`  | No    | Create an account → returns JWT      |
| POST   | `/api/auth/login`     | No    | Login → returns JWT                  |
| GET    | `/api/tasks`          | JWT   | List the **current user's** tasks    |
| GET    | `/api/tasks/{id}`     | JWT   | Get one of the user's tasks          |
| POST   | `/api/tasks`          | JWT   | Create a task                        |
| PUT    | `/api/tasks/{id}`     | JWT   | Update title / description / status  |
| DELETE | `/api/tasks/{id}`     | JWT   | Delete a task (204 No Content)       |

### Example login response
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "ahmed@example.com",
  "fullName": "Ahmed Hassan"
}
```

### Error format (example — validation)
```json
{
  "timestamp": "2026-09-22T10:00:00Z",
  "status": 400,
  "error": "Validation failed",
  "message": "Invalid request",
  "details": { "email": "Email must be valid" }
}
```

---

## 🧪 Running the Tests

```bash
mvn test
```
Tests use an **H2 in-memory** database (MySQL compatibility mode) —
no database server required. Includes:
- `contextLoads` — the full Spring context boots correctly
- `JwtServiceTest` — token generation, username extraction, and validation

---

## 💡 What This Project Demonstrates (interview talking points)

1. **REST API design** — clear resource endpoints, DTOs, proper HTTP status codes.
2. **JWT security end-to-end** — token creation, signing (HS256), a `OncePerRequestFilter`,
   and stateless `SecurityFilterChain` configuration.
3. **Layered & clean architecture** — separation of concerns, easy to test & extend.
4. **Database persistence** — JPA entities, `@ManyToOne` ownership, derived queries on
   `JpaRepository`, and switching between **MySQL and SQL Server** via profiles.
5. **Error handling** — global `@RestControllerAdvice` with consistent JSON errors.
6. **Security best practices** — BCrypt hashing, token expiry, owner-based authorization,
   no credentials in logs, configurable secret.

---

## 📝 License

Free to use and extend for personal projects, learning, and job applications.