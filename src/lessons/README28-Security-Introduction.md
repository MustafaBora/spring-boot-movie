# Security Concepts: The Problems Before the Solutions

## Where We Are

Before adding security, here is the full lifecycle of a request through the application we have built so far:

```
Client
  → Controller          (HTTP, routing, @Valid)
  → DTO                 (input shape, validation annotations)
  → Service             (business logic)
  → Repository          (database query)
  → DB

DB
  → Entity              (JPA object)
  → MapStruct           (Entity → DTO)
  → DTO                 (output shape)
  → ResponseEntity      (HTTP status + body)
  → Client
```

And if anything goes wrong along the way:
```
Exception thrown (anywhere)
  → GlobalExceptionHandler   (@RestControllerAdvice)
  → ResponseEntity with error body
  → Client
```
We will see the last part soon.

This file starts adding **security** on top of that pipeline. Security does not replace any of the above — it sits in front of the controller as an additional layer.

---

Before we dive into the Spring implementation, we need to understand the **problems** we are trying to solve.
Forget Spring for a moment. We have a REST API. What security problems do we face?

---

## 1. Authentication vs Authorization

These two words are often confused. The difference is fundamental.

| Concept | Question | Example |
|---|---|---|
| **Authentication** | Who are you? | Prove your identity with email + password |
| **Authorization** | What are you allowed to do? | An ADMIN can manage all data, a USER can only see their own |

Authentication always comes first. We cannot decide what someone is allowed to do until we know who they are.

## 🧠 Quick model

Authentication = login  
Authorization = permissions

👉 Example:
Login → you are Gomathi  
Authorization → Gomathi is ADMIN

Login → you are Kien
Authorization → Kien is USER


---

## 2. How Should Passwords Be Stored?

When we register a user, what do we do with the password?

### Wrong: Storing plain text
```
users table:
email: basel@email.com
password: 12345
```
If the database is breached, every user's password is immediately exposed.

### Wrong: Encrypting
Encryption is **reversible** — it is a two-way transformation.

> **Analogy:** Encryption is like a locked box with a key. Anyone who has the key can open the box and read what is inside.

You encrypt with a key, and you can decrypt with the same (or a related) key. If an attacker obtains the key, every stored password is exposed at once.

Common encryption algorithms: **AES**, **RSA**, **3DES**.

Encryption has legitimate uses (e.g. encrypting data in transit, encrypting files), but it is the **wrong tool for storing passwords**.

### Correct: Hashing
A **hash function is one-way**. You can compute the output from the input, but you cannot reverse it.

> **Analogy:** Hashing is like putting a letter through a shredder. You can always shred the same letter and get the same pile of pieces, but you cannot reconstruct the original letter from the shreds.

When a user registers, we hash their password and store only the hash:

```
"12345"  →  hash()  →  "$2a$10$XqP9..."
```

At login:
- The user submits their password
- We run the same hash function
- We **compare** the result against the stored hash — we never store or compare the raw password
- If they match, the password is correct

Common hashing algorithms: **SHA-256**, **SHA-3**, **MD5** (outdated, no longer safe), **bcrypt**, **argon2**.
For passwords specifically, **bcrypt** and **argon2** are preferred because they are intentionally slow — making brute-force attacks much harder.

### What is a Salt?

If the same password always produces the same hash, attackers can use pre-computed tables of hashes — called **rainbow tables** —
to reverse common passwords.

> **Analogy:** Imagine you know that "password123" always shreds into a specific pile. You build a catalogue of piles for all common passwords. When you see a matching pile in a stolen database, you immediately know the original word. A salt ruins this — it is like mixing a unique handful of confetti into the letter before shredding, so the same letter always produces a different pile.

The fix: before hashing, we add a random value called a **salt** to the password.

```
"12345" + "x9fK2"  →  hash()  →  "$2a$10$abc..."
"12345" + "mN7qP"  →  hash()  →  "$2a$10$xyz..."
```

The same password now produces a **different hash every time**. Rainbow table attacks become useless.

---

## 3. Stateful vs Stateless — How Do We Remember Who Is Logged In?

HTTP is a **stateless protocol** — every request is independent.
A user just logged in. How does the server recognise them on the next request?

### Approach 1: Sessions (Stateful)

1. The user logs in.
2. The server creates a **session** and stores it in memory or a database.
3. The server sends the user a **session ID** (usually as a cookie).
4. On every request, the browser automatically sends the cookie.
5. The server looks up the session ID and recognises the user.

```
Server memory:
SESSION_ID: "abc123"  →  { userId: 42, role: "ADMIN" }
```

**Problem:** The server holds state. If we run two servers, which one has the session?
With millions of users, this table grows very large and becomes a bottleneck.

---

### Approach 2: Tokens (Stateless)

1. The user logs in.
2. The server generates a **token** that contains user information.
3. The token is **signed** so the server can verify it created the token.
4. The token is sent to the user. **The server stores nothing.**
5. On every request, the user sends the token in the `Authorization` header.
6. The server **verifies** the token by checking the signature and reads the user information from it.

```
On every request:
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Advantage:** The server remembers nothing. We can add as many servers as we need.

| | Sessions | Tokens |
|---|---|---|
| Server stores state? | Yes | No |
| Scalability | Difficult | Easy |
| Database query per request? | Yes (session lookup) | No |
| If the credential is stolen? | Session can be deleted | Valid until it expires |

---

## The Big Picture for Next Lessons

Every topic on next week answers one of these questions:

```
1. How do we store passwords safely?
   → Password Hashing (BCrypt)

2. How does a user prove who they are?
   → Registration & Login endpoints

3. How do we carry identity after login?
   → JWT token generation and validation

```

---

## Summary

Before moving forward, these are the concepts we need to be clear on:

| Concept | In short |
|---|---|
| Authentication | Proving identity |
| Authorization | Checking what we are allowed to do |
| Hashing | One-way transformation — cannot be reversed |
| Salt | A random value added before hashing so the same password produces a different hash every time |
| Stateless | The server stores nothing between requests |

The Spring implementation of all of these will be covered step by step.
