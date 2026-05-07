# Security Concepts Continued: JWT, Filters, and CSRF

This file continues from README28. The sections here are numbered 4–6, picking up where that file left off.

---

## 4. JWT — What Is Inside the Token?

A **JWT (JSON Web Token)** has three parts, separated by dots:

```
eyJhbGciOiJIUzI1NiJ9  .  eyJ1c2VySWQiOjQyfQ  .  SflKxwRJSMeKKF2QT4f
       HEADER                    PAYLOAD               SIGNATURE
```

| Part | Contents | Example |
|---|---|---|
| **Header** | Token type and algorithm | `{ "alg": "HS256" }` |
| **Payload** | User data (claims) | `{ "userId": 42, "role": "ADMIN", "exp": 1713600000 }` |
| **Signature** | Proof the server issued this token | Only the server can verify it |

**Important:** JWT is **not encrypted** — the payload is Base64-encoded, meaning anyone can decode and read it.
We must never put sensitive data (passwords, credit card numbers) in a JWT payload.

Security comes from the **signature** — if anyone modifies the payload, the signature becomes invalid.

### Token Expiration

Tokens can be stolen. To limit the damage, every token carries an **expiration time**:

```json
{
  "userId": 42,
  "exp": 1713600000
}
```

> `exp` is a **Unix timestamp** — the number of seconds elapsed since January 1, 1970. The value above represents a specific date and time. An online Unix timestamp converter can turn it into a human-readable date.

An expired token is rejected by the server. The user must log in again to get a fresh token.

---

## 5. Securing Endpoints — Who Can Access What?

In most APIs, some endpoints are public and some are protected:

```
GET  /movies          →  Anyone can access (public)
POST /movies          →  Only ADMIN
GET  /auth/login      →  Anyone (of course)
GET  /user/profile    →  Only authenticated users
```

Writing `if (user == null) return 401;` in every controller method:
- Repeats the same logic everywhere (violates DRY)
- Easy to forget on one endpoint
- Hard to test consistently

The solution: requests should be checked in a **dedicated layer before they reach the controller**.
This layer intercepts every request, validates the token, and checks permissions.

In Spring, this is done with a **Filter** — we will see the implementation in the next files.

---

## 6. CSRF — Another Site Making Requests on Our Behalf

**CSRF (Cross-Site Request Forgery):** A malicious website tricks a user's browser into sending requests to a trusted API where the user is already authenticated.

How it works:

```
1. A user logs into bank.com — session cookie is saved in the browser
2. The user visits a malicious website
3. The malicious site silently submits:
   <form action="https://bank.com/transfer" method="POST">
     <input name="amount" value="5000">
     <input name="to" value="hacker">
   </form>
4. The browser automatically attaches the bank.com cookie
5. The bank processes the request as if the real user sent it
```

### Is CSRF a problem with JWT?

| Auth Mechanism | CSRF Risk |
|---|---|
| Session cookie | High |
| JWT stored in a cookie | High |
| JWT in Authorization header | None |

Browsers automatically attach cookies to every request — but they cannot attach an `Authorization` header automatically.
Carrying our JWT in the `Authorization` header eliminates CSRF by design.

---

## The Big Picture for the Next lessons

4. How do we check identity on every request?
   → Spring Security Filter Chain

5. Who is allowed to access which endpoint?
   → SecurityConfig with authorisation rules

6. How do we protect against CSRF?
   → JWT carried in the Authorization header

## Summary

Before moving forward, these are the concepts we need to be clear on:

| Concept | In short |
|---|---|
| JWT | A signed token that carries user information |
| Expiration | The time limit on a token's validity |
| Filter | A security layer that runs before the request reaches the controller |
| CSRF | A malicious site making requests on behalf of the user |

The Spring implementation of all of these will be covered step by step.
