# Securing Endpoints with Spring Security

So far we have covered the concepts — authentication, authorization, JWT tokens, and the idea that every request should pass through a validation layer before it reaches the controller.

Now we put all of that into practice using **Spring Security**.

Spring Security is a library that plugs directly into the Spring request pipeline. Once added, it intercepts every incoming HTTP request **before** it reaches any controller. We configure it to decide two things:

- which routes are **public** (no token needed — e.g. `/auth/login`)
- which routes are **protected** (a valid JWT is required)

That configuration lives in a single bean called `SecurityFilterChain`. We will build it step by step in this file.

---

Spring Security controls **who** can access **which** endpoints.

---

## Public vs Protected Endpoints

| Route            | Description           | Access                         |
|------------------|-----------------------|--------------------------------|
| `/auth/register` | User signup           | Public                         |
| `/auth/login`    | User login            | Public                         |
| `/api/**`        | Application resources | **Protected (requires token)** |

---

## Core Security Steps

1. **Disable** default Spring Security login UI.
2. Add a **JWT authentication filter** that:
   - Reads `Authorization` header
   - Validates token
   - Loads user identity
3. Mark protected endpoints so only authenticated users may access them.

---

## What is happening here?

With security in place, every request follows this path:

```
Client
  → Security Filter          (reads Authorization header, validates JWT, sets user identity)
  → Controller               (only reached if token is valid)
  → Service                  (business logic, runs as the authenticated user)
  → Repository → DB

Spring Security works like a pipeline.
```

If the token is missing or invalid, the request is rejected **at the filter** — the controller never runs:

```
Client
  → Security Filter          (token missing or invalid)
  → 401 Unauthorized         (returned immediately, nothing else executes)
```

The JWT filter:
- reads the `Authorization: Bearer <token>` header
- validates the signature and expiration
- loads the user identity into Spring's `SecurityContext`

Once the user identity is in the `SecurityContext`, the rest of the application (service, repository) can access it without the controller needing to pass it manually.

---

## High-level Example (Conceptual)

```java
http
  .csrf(csrf -> csrf.disable())
  .authorizeHttpRequests(auth -> auth
    .requestMatchers("/auth/**").permitAll()  // public
    .anyRequest().authenticated()             // protected
  )
  .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
```

**`.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)`** — Spring Security runs a chain of filters before a request reaches the controller. `UsernamePasswordAuthenticationFilter` is the built-in filter for processing form-based login (username + password field). By inserting our JWT filter *before* it, we authenticate every incoming request via the token before Spring's default mechanism runs.

---

## Result

✅ Only users with **valid JWT tokens** can call protected API endpoints.
❌ Requests without valid tokens receive `401 Unauthorized`.

---