# 🛒 E-Commerce Spring Boot — Day 1 Summary

**Date:** February 24, 2026

---

## What I Accomplished

Today was focused on **planning, architecture, and project setup** before writing any real feature code. A solid day of
groundwork that will pay off throughout the build.

---

## Architecture & Planning

- Decided to build a full e-commerce REST API using **Spring Boot 3**, **Spring Security (JWT)**, **Spring Data JPA**,
  and **PostgreSQL**
- Chose a **package-by-layer** project structure under `me.nranz.ecommerce` with the following packages:
    - `controllers` + `controllers/admin`
    - `services`
    - `repositories`
    - `models` + `models/enums`
    - `dto/request` + `dto/response`
    - `security`
    - `exceptions`

---

## Jira Setup

Created **6 Epics** and **13 Stories** in the **Team Nranz (SCRUM)** Jira project:

| Epic                     | Key      | Stories                      |
|--------------------------|----------|------------------------------|
| User Auth & Registration | SCRUM-5  | SCRUM-11, SCRUM-12, SCRUM-13 |
| Product Catalog          | SCRUM-6  | SCRUM-14, SCRUM-15           |
| Shopping Cart            | SCRUM-7  | SCRUM-16, SCRUM-17           |
| Orders & Checkout        | SCRUM-8  | SCRUM-18, SCRUM-19           |
| Payments (Stripe)        | SCRUM-9  | SCRUM-20, SCRUM-21           |
| Admin Management         | SCRUM-10 | SCRUM-22, SCRUM-23           |

---

## Key Decisions Made

- **Start with Auth (SCRUM-5)** — Spring Security is hard to retrofit, so getting JWT auth right first means every
  feature built after it is already secured
- **JWT-only auth for now** — building the auth layer manually first to understand the fundamentals before abstracting
  it away
- **Keycloak migration path identified** — once the app matures, users can be migrated to a self-hosted Keycloak server.
  BCrypt passwords can be migrated via Keycloak's password-on-login rehashing feature. Only the `security` package would
  need to change; controllers and services remain untouched
- **DTOs over direct entity exposure** — never expose JPA entities (especially `User`) directly in API responses

---

## Concepts Learned

### JWT vs OAuth2

- **JWT** is a token *format* — a self-contained signed string encoding user claims
- **OAuth2** is an authorization *protocol/framework* — defines the flow for third-party access (e.g. "Login with
  Google")
- For this project: JWT-only now, OAuth2/Keycloak optionally later

--------------------------------------------------------------------------------------

# 🛒 E-Commerce Spring Boot — Day 2 Summary

**Date:** February 26, 2026

---

## Overview

Today focused on cleaning up the JWT authentication code written on Day 1 and establishing a solid architectural
foundation for the rest of the project. The main themes were **clean layered architecture**, **DTOs**, **request
validation**, and **global exception handling**.

---

## Controller → Service → Repository → Database

Intentionally structured the codebase around this flow for the first time, ensuring each layer has a single
responsibility and doesn't leak concerns into adjacent layers.

---

## Data Transfer Objects (DTOs)

This was the first time intentionally designing and implementing DTOs rather than passing entities directly between
layers.

**Key insight:** it's bad practice for incoming and outgoing API objects to directly mirror your internal database
schema.

|            | Purpose                                                                   |
|------------|---------------------------------------------------------------------------|
| **Entity** | Represents how data is *stored* in the database                           |
| **DTO**    | Represents how data is *transferred* between layers or exposed externally |

**Why DTOs matter:**

- **Incoming request DTOs** can be validated and shaped specifically for the workflow they serve
- **Outgoing response DTOs** expose only the necessary data — e.g. excluding sensitive fields like passwords
- **The API contract is decoupled from the database schema**, meaning the database structure can evolve without
  breaking the API

---

## Deepened Understanding of JPA

Learned how Spring Data JPA parses method names directly into SQL queries:

```java
// SELECT COUNT(*) > 0 FROM user WHERE username = ? OR email = ?
boolean existsByUsernameOrEmail(String username, String email);

// SELECT * FROM user WHERE active = false
List<User> findByActiveFalse();
```

For more complex queries, the `@Query` annotation allows writing JPQL or native SQL directly above the method:

```java

@Query("SELECT u FROM User u WHERE u.email = :email AND u.active = true")
Optional<User> findActiveUserByEmail(@Param("email") String email);
```

---

## Global Exception Handling

Implemented a `@RestControllerAdvice` to centralize all error handling across the application, covering:

- **Custom exceptions** — e.g. `UserAlreadyExistsException`
- **Validation failures** — e.g. `MethodArgumentNotValidException`
- **Database constraint violations**

All errors return a standardized response structure:

```json
{
  "timestamp": "2026-02-26T10:30:00",
  "status": 409,
  "error": "Conflict",
  "message": "User with this email already exists",
  "path": "/api/auth/register"
}
```

This means no endpoint needs its own try/catch — error handling is handled in one place for the entire app.

---

## Request Validation with Jakarta Bean Validation

Added validation annotations directly to DTOs so that incoming requests are validated *before* any service logic
executes — no manual if/else checks needed in the controller or service layer.

```java
public class RegisterRequest {

    @NotBlank
    private String username;

    @Email
    @NotBlank
    private String email;

    @Size(min = 8, max = 64)
    @NotBlank
    private String password;
}
```

**How it works end-to-end:**

1. Request hits the controller with `@Valid` on the request body parameter
2. Spring automatically validates the DTO against its annotations
3. If validation fails, Spring throws `MethodArgumentNotValidException`
4. The `@RestControllerAdvice` catches it and returns a clean, structured error response

See `LoginRequest.java` for more annotation examples.

---

## Key Takeaways

- DTOs decouple the API contract from the database schema — a change to the `User` entity doesn't automatically
  break the registration endpoint
- Validation belongs on the DTO, not scattered across service methods
- A single `@RestControllerAdvice` keeps error handling consistent and eliminates boilerplate across all controllers
- Spring Data's method name parsing is powerful for simple queries; `@Query` handles anything more complex

---

# 🛒 E-Commerce Spring Boot — Day 3 Summary

## ✅ Accomplished Today

### Bug Fixes

- **Fixed JWT issuer mismatch** — `withIssuer()` was commented out in token generation but still required in validation,
  causing all token validations to fail
- **Fixed empty error response body** — replaced `response.sendError()` with direct JSON writes so Postman returns
  meaningful error messages on failed auth
- **Temporarily patched SecurityConfig** — changed `.hasRole("USER")` to `.authenticated()` since user roles are not yet
  implemented (tracked in SCRUM-24)

### Feature Progress (SCRUM-13 — View and Update User Profile)

- Reviewed existing `GET /api/user/info` endpoint — aligns with acceptance criteria for `GET /api/users/me`
- Began building `updateUserProfile()` in `UserService`
- Identified and fixed bug where a new `User` object was being created instead of loading and updating the existing one
- Discussed and created `UpdateUserProfileRequest` and `UpdateUserProfileResponse` DTOs

---

## 📚 Concepts Learned Today

### Spring Security Request Flow

```
HTTP Request → JWTFilter → SecurityFilterChain → Controller
```

- `SecurityConfig` is a rulebook written at startup, not per request
- The filter chain enforces those rules on every request

### JWT Filter Breakdown

- Token signature, subject, and issuer are validated in `JwtUtil`
- User existence is confirmed via `loadUserByUsername()` in the filter
- Only after both pass is the authentication context written to `SecurityContextHolder`
- `SecurityContextHolder` is wiped after every request (stateless)

### Authentication Context (SecurityContextHolder)

- Acts as a per-request store Spring Security reads to make access decisions
- `UsernamePasswordAuthenticationToken(username, null, authorities)` — 3 args signals already authenticated
- `UsernamePasswordAuthenticationToken(username, password)` — 2 args signals needs verification (used at login)
- Password should be passed as `null` in the filter — credentials aren't needed after JWT validation
- The badge analogy was refined: it's simply the **authentication context** for the current request

### Stateless vs Stateful Auth

|                                | Stateful (Sessions) | Stateless (JWT) |
|--------------------------------|---------------------|-----------------|
| Server stores session          | ✅ Yes               | ❌ No            |
| Scales across multiple servers | ❌ Hard              | ✅ Easy          |
| Can invalidate token early     | ✅ Yes               | ❌ Not easily    |

- JWT expiry + refresh tokens is the standard solution to the invalidation problem (tracked in backlog)

### DTOs (Data Transfer Objects)

- Control exactly what fields are accepted in requests and returned in responses
- Prevent clients from sending sensitive fields like `role` or `password` in update requests
- Tradeoff: tight coupling to endpoints means updating the data model requires updating DTOs too
- Decision made to continue using DTOs — security and control benefits outweigh maintenance cost

### Identity Providers (Cognito, Entra ID)

- Everything built manually today (filter, JwtUtil, UserDetailsService, SecurityConfig) is handled automatically by IdP
  SDKs
- Building it manually is valuable — you now understand what those SDKs do under the hood

### SecurityContext vs Bearer Token

- Spring Security doesn't understand JWT natively — it only understands the authentication context
- The filter acts as a translation layer: JWT → authentication context
- This means swapping auth providers (e.g. to Cognito) only requires changing the filter, not Spring Security rules

### Pulling Username from SecurityContext

- Correct pattern for authenticated endpoints — never trust username from request body
- User can't manipulate the SecurityContext since it was set by the validated JWT

### DB Update Pattern

- Load → Update → Save (2 DB calls, updates all columns) vs direct `@Query` update (1 DB call, updates specific columns)
- For low-frequency endpoints like profile updates, Load → Update → Save is acceptable
- Direct queries are better for high-frequency or bulk operations

---

## 🗂 Backlog Updates

| Ticket   | Summary                                             | Status |
|----------|-----------------------------------------------------|--------|
| SCRUM-24 | Implement User Roles (ROLE_USER / ROLE_ADMIN)       | To Do  |
| SCRUM-25 | Implement JWT Expiration and Refresh Token Workflow | To Do  |

---

## 📅 Up Next — Day 4

- Write first unit tests using JUnit 5 + Mockito against `UserService`
- Complete SCRUM-13 (update profile endpoint)
- Consider starting product catalog
