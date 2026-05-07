# JWT (JSON Web Token) Authentication

> The JWT structure (header, payload, signature) and the theory behind stateless authentication were covered in README35. This file focuses on the authentication flow and how the token is used in practice.

## JWT Authentication Steps

1. User logs in successfully.
2. Server generates and signs JWT token.
3. Client stores token:
   - **`localStorage`** (browser): Simple but accessible to JavaScript — vulnerable if the page has an XSS vulnerability.
   - **`HttpOnly` cookie**: JavaScript cannot access it, but re-introduces CSRF risk (see README46).
   - **In memory** (mobile apps or backend services): held in a variable, never written to disk — disappears on page reload or app restart.

   **Where should they store it?**

   | Storage | XSS risk | CSRF risk | Survives page reload |
   |---|---|---|---|
   | `localStorage` | ✅ Yes | ❌ No | ✅ Yes |
   | `HttpOnly` cookie | ❌ No | ✅ Yes | ✅ Yes |
   | In memory | ❌ No | ❌ No | ❌ No |

   There is no perfect option — every choice is a trade-off between two different attack vectors.
   The most common approach for **REST APIs we build in this course** is `localStorage`, because:
   - it is simple to use from any frontend framework
   - CSRF is not a concern when using the `Authorization: Bearer` header (the browser never adds it automatically)
   - the XSS risk is managed by keeping the frontend code clean and avoiding untrusted input in the DOM

   `HttpOnly` cookies are preferred when security requirements are very strict and a proper CSRF protection layer is added.

4. For every request, client includes token in `Authorization` header.
5. Server:
   - Validates the token
   - Extracts user identity
   - Allows or denies access
---

## Think of JWT like an ID card

Server says:
"Here is your ID card"
Client says on every request:
"Here is my ID card"

Server verifies:

✔ valid signature  
✔ not expired  
✔ trusted issuer

---

## Why JWT Instead of Sessions?

| Sessions                             | JWT                                     |
|--------------------------------------|-----------------------------------------|
| Server stores session                | Server stores nothing (stateless)       |
| Harder to scale across servers       | Easy to scale (no shared session store) |
| Works best with traditional web apps | Works best with modern SPAs & APIs      |

---

## Why stateless matters

Session:
Server remembers you

JWT:
Client proves identity every time

👉 This removes:
- server memory load
- session synchronization problems

---

## Renewing Expired Tokens

Tokens expire — that is intentional. But we do not want users to be kicked out and forced to log in again every 15 minutes.

The standard solution is to use **two tokens**:

| Token | Lifetime | Purpose |
|---|---|---|
| **Access token** | Short (e.g. 15 min) | Sent with every request, authorizes API calls |
| **Refresh token** | Long (e.g. 7 days) | Stored securely, used only to get a new access token |

The flow looks like this:

```
1. User logs in
   → Server returns: access token (15 min) + refresh token (7 days)

2. Client uses the access token for every request
   → Works fine while it is valid

3. Access token expires
   → Server responds with 401 Unauthorized

4. Client sends the refresh token to POST /auth/refresh
   → Server validates the refresh token
   → Server issues a new access token (and optionally a new refresh token)

5. Client retries the original request with the new access token
```

This way the user stays logged in for days without re-entering their password, but if the access token is stolen it becomes useless within minutes.

> **What happens if the refresh token is stolen?**  
> The attacker can keep getting new access tokens. This is why refresh tokens are stored more carefully (e.g. `HttpOnly` cookie or secure storage), and servers often maintain a list of valid refresh tokens so they can be revoked (e.g. on logout or suspicious activity).

---

## What Comes Next

The concepts above explain *what* JWT is and why we use it. The Spring Security implementation — generating tokens, writing the filter that validates them, and configuring endpoint access — is covered in README44.

---

