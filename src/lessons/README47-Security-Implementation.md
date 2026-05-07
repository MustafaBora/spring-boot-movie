# Implementing Security — From Concepts to Code

> This file bridges the theory from README28, README35, README42, README44, and README46 into a single implementation blueprint.  
> The complete working code for the movie application lives in [`info/movie-security-package.md`](../../info/movie-security-package.md).

---

## What We Need to Build

We need three things to secure a Spring Boot REST API with JWT:

| Class | Responsibility |
|---|---|
| `JwtUtils` | Create tokens and validate them |
| `JwtAuthenticationFilter` | Intercept every request, read the token, identify the user |
| `SecurityConfig` | Tell Spring which routes are public and which are protected |

Before we look at each class, we need to understand **roles**.

---

## Roles — What They Are and Why We Need Them

A **role** is a label we attach to a user that describes what they are allowed to do.

Without roles, "is the user logged in?" is the only question we can ask.  
With roles, we can ask finer questions: "is this user an admin?", "is this a verified account?", "is this a premium member?"

In our movie API we only need two:

```java
public enum Role {
    USER,   // can view movies
    ADMIN   // can create, update, and delete movies
}
```

Other real-world examples:

| App | Roles |
|---|---|
| E-commerce | `CUSTOMER`, `SELLER`, `SUPPORT`, `ADMIN` |
| Hospital system | `PATIENT`, `DOCTOR`, `NURSE`, `RECEPTIONIST` |
| School platform | `STUDENT`, `TEACHER`, `PARENT`, `ADMIN` |
| Content platform | `VIEWER`, `CREATOR`, `MODERATOR`, `ADMIN` |

The role is stored on the `User` entity and can also be embedded in the JWT token when the user logs in.

In this implementation, we still load the user from the database to build the principal. This keeps the `SecurityContext` based on the current database state.

---

## Step 1 — JwtUtils

`JwtUtils` is a plain `@Component`. It knows one thing: how to create and read JWT tokens.

### What it needs

- A **secret key** — a long random string kept in `application.properties` (never in source code)
- An **expiration time** — how many milliseconds until the token expires

```
app.jwt.secret=<base64-encoded-random-string>
app.jwt.expiration=900000   # 15 minutes in milliseconds
```

### What it does

**Generate a token** — given a subject (email) and a map of extra claims (userId, role), produce a signed JWT string:

```
pseudo-code:
  now = current time
  exp = now + expiration duration

  token = header(HS256)
        + payload(subject, claims, issuedAt, expiration)
        + signature(key)
```

**Validate a token** — parse it with the same key, catch any exception (expired, tampered, malformed) and return true/false:

```
pseudo-code:
  try
    parse token with key
    check expiration > now
    return true
  catch any JwtException
    return false
```

**Extract the subject** — after validation, read the `sub` field (email) so we know who made the request:

```
pseudo-code:
  parse token → get body → get subject (email)
```

> The secret key must be long enough for HMAC-SHA256 (at least 256 bits = 32 bytes). We generate it once, Base64-encode it, and store it as a property.

---

## Step 2 — JwtAuthenticationFilter

This filter runs on **every single HTTP request** — before the controller, before the service, before anything else.

Its job is simple: if the request carries a valid JWT, identify the user and tell Spring Security who they are. If not, do nothing — just let the request continue (Spring Security will then block it at the route-level check if the route is protected).

### The filter logic, step by step

```
pseudo-code:
  read "Authorization" header from the request

  if header is null or does not start with "Bearer "
    skip (pass request to the next filter)

  extract the token (remove "Bearer " prefix)

  if token is NOT valid
    skip (pass request to the next filter)

  extract email from token
  load User from the database by email

  if user found
    build a Spring Security Authentication object
      principal  = the User entity
      authorities = ["ROLE_USER"] or ["ROLE_ADMIN"]

    store it in SecurityContextHolder

  pass request to the next filter (chain.doFilter)
```

Two important points:

1. **We never return a 401 here ourselves.** We just set (or don't set) the authentication in the context. Spring Security's route rules (configured in `SecurityConfig`) decide whether to allow or reject the request after the filter chain completes.

2. **We always call `chain.doFilter`** — even when the token is missing or invalid. A missing token on a public route (`/auth/login`) is perfectly fine. The filter should not block those.

> For SKIP, we just call `chain.doFilter(request, response)` without setting anything in the context. The request continues as if the filter wasn't there.

> Some applications directly return 401 for invalid tokens. In this beginner-friendly version, we simply leave the request unauthenticated and let Spring Security handle the final decision.


### What goes into `SecurityContextHolder`

```java
// What we build and store:
UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
    user,             // principal  — the logged-in User object, accessible later in controllers
    null,             // credentials — not needed after authentication
    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
);

SecurityContextHolder.getContext().setAuthentication(auth);
```

After this line, any code later in the request (service, controller) can call:

```java
SecurityContextHolder.getContext().getAuthentication().getPrincipal()
```

to get the current user — without it being passed as a parameter.



> **Why "Authentication" and not "Authorization"?**
>
> This trips up almost everyone at first. The difference comes back to the definitions from README28:
>
> - **Authentication** = *who are you?* — proving identity
> - **Authorization** = *what are you allowed to do?* — checking permissions
>
> What does our filter actually do? It reads the JWT, verifies it, loads the user from the database, and stores an `Authentication` object in `SecurityContextHolder`. At the end of the filter, Spring knows *who* is making the request. It has not yet decided *what they can do*.
>
> The *what they can do* check happens **after** the filter chain, inside `SecurityConfig` route rules (`.hasRole("ADMIN")`, `.authenticated()`, etc.). That is the authorization step.
>
> So the name is correct: the filter performs **authentication** (identity), and `SecurityConfig` performs **authorization** (permissions).

### A real application can have more than one filter
`JwtAuthenticationFilter` is not the only filter we might need. Each filter should do exactly one thing, and they are chained together — every request passes through all of them in order.

Common examples alongside our JWT filter:

| Filter | Responsibility |
|---|---|
| `CsrfFilter` | Spring's built-in CSRF protection filter. We disable it for this stateless JWT API. |
| `JwtAuthenticationFilter` | Read the token, identify the user, populate `SecurityContextHolder` |
| `RequestLoggingFilter` | Log method, path, authenticated user, and response time for every request |
| `RateLimitingFilter` | Count requests per IP or per user and reject with 429 if a threshold is exceeded |

### How we control filter order

Filter order is defined in `SecurityConfig` using `addFilterBefore` and `addFilterAfter`. Both methods take the filter to add and a reference filter that marks its position in the chain:

```java
http
    // Our JWT filter runs before Spring's built-in username/password filter
    .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

    // A logging filter runs after the JWT filter so it can read the authenticated user
    .addFilterAfter(requestLoggingFilter, JwtAuthenticationFilter.class);
```

The chain for a typical request then looks like:

```
Incoming request
  → JwtAuthenticationFilter   (validates token, sets user in SecurityContext)
  → RequestLoggingFilter      (can now read the authenticated user)
  → later Spring Security checks  (route rules: public vs protected, role checks)
  → Controller
```

> Rule of thumb: place filters that **identify the user** early, and filters that **use the identity** (logging, auditing, rate limiting per user) after.

### Note — where would a Refresh Token mechanism go?

If we wanted to support the access token + refresh token pattern from README42, here is where each piece would live:

```
RefreshToken entity / table
  → stores: tokenString, userId, expiresAt, revoked flag
  → managed by: RefreshTokenRepository + RefreshTokenService

POST /auth/refresh  (new public endpoint in AuthController)
  → receives the refresh token (request body or HttpOnly cookie)
  → RefreshTokenService validates: not expired, not revoked
  → JwtUtils.generateToken(...)  issues a new access token
  → optionally rotates the refresh token (old one revoked, new one issued)
  → returns the new access token to the client

POST /auth/logout
  → RefreshTokenService marks the refresh token as revoked
  → client discards the access token on its side
```

The `JwtAuthenticationFilter` itself does **not** change — it only validates access tokens. The refresh flow is a separate HTTP round-trip initiated by the client, not something the filter handles transparently.

*"Transparently"* here means: without the client knowing it happened. Some systems intercept a 401, automatically call `/auth/refresh` behind the scenes, and retry the original request — all inside the filter. We deliberately avoid that pattern. Our filter has one job (identify the user), and the client is responsible for deciding when to refresh. Mixing both concerns in one filter makes it harder to reason about and harder to test.

---

## Step 3 — SecurityConfig

`SecurityConfig` is a `@Configuration` class that contains two `@Bean` methods.

### Bean 1 — Password encoder

```java
@Bean
public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

We register this once here so it can be injected anywhere (e.g. in the registration service that hashes the password before saving).

### Bean 2 — SecurityFilterChain

This is where we define the rules. The shape of the configuration is always the same:

```
pseudo-code:
  disable CSRF                (safe because we use the Authorization header, not browser-sent cookies)
  set session management = stateless  (we do not store sessions; JWT is stateless)

  route rules:
    /auth/**              → anyone (permitAll)
    GET /movies/**        → anyone (permitAll)
    POST /movies/**       → only ADMIN role
    PUT /movies/**        → only ADMIN role
    DELETE /movies/**     → only ADMIN role
    anything else         → must be authenticated

  add our JwtAuthenticationFilter BEFORE Spring's built-in filter
```

The `addFilterBefore` line is important. Spring Security has its own internal filter (`UsernamePasswordAuthenticationFilter`) for form-based login. By inserting our filter *before* it, we ensure that every request is checked for a JWT first.

### What `hasRole("ADMIN")` actually checks

When we write:

```java
.requestMatchers(HttpMethod.GET, "/movies/**").permitAll()
.requestMatchers(HttpMethod.POST, "/movies/**").hasRole("ADMIN")
.requestMatchers(HttpMethod.PUT, "/movies/**").hasRole("ADMIN")
.requestMatchers(HttpMethod.DELETE, "/movies/**").hasRole("ADMIN")
.anyRequest().authenticated()
```

Spring Security looks for `"ROLE_ADMIN"` in the user's authorities list. (We can reach a users authorities list by calling `SecurityContextHolder.getContext().getAuthentication().getAuthorities()`.  )
That is why in the filter we prefix the role name: `"ROLE_" + user.getRole().name()`. Otherwise `hasRole("ADMIN")` would look for `"ADMIN"` instead of `"ROLE_ADMIN"` and fail.

| What we store in DB | What we put in authorities | What Spring checks |
|---|---|---|
| `ADMIN` | `ROLE_ADMIN` | `hasRole("ADMIN")` |
| `USER` | `ROLE_USER` | `hasRole("USER")` |

Authorities and roles are conceptually the same thing in Spring Security. The `hasRole` method is just a convenient way to check for an authority with a "ROLE_" prefix.

---

## How the Three Classes Connect

```
Incoming request
  │
  ▼
JwtAuthenticationFilter
  │  reads "Authorization: Bearer <token>"
  │  calls JwtUtils.isValid(token)
  │  calls JwtUtils.getSubject(token)  → email
  │  loads User from DB
  │  stores User + role in SecurityContextHolder
  │
  ▼
SecurityConfig route rules
  │  checks: is this route public or protected?
  │  checks: does the authenticated user have the required role?
  │
  ├─ allowed → Controller → Service → Repository
  └─ denied  → 401 Unauthorized (no token) or 403 Forbidden (wrong role)
```

---

## Summary Checklist

> **One sentence to burn into memory:** the filter identifies *who* is making the request; `SecurityConfig` decides *what* they are allowed to do.

Before looking at the full code, we should be able to answer these:

- [ ] What is a role and where is it stored?
- [ ] What three things does `JwtUtils` need to do?
- [ ] What does the filter do when the token is valid? When it is missing?
- [ ] Why do we call `chain.doFilter` even when there is no token?
- [ ] Why do we prefix role names with `"ROLE_"`?
- [ ] Why do we disable CSRF?
- [ ] What does `addFilterBefore` do and how does `addFilterAfter` differ?
- [ ] If we had two filters, which one should run first and why?
- [ ] Where would the refresh token endpoint and its service live?

Once these are clear, the full implementation in [`movie-security-package.md`](../../info/movie-security-package.md) should read like a natural translation of these concepts into Java.
