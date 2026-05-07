# Exception Handling in Spring Boot

When a user requests a resource that does not exist, we need to communicate that clearly
via the HTTP response — not with a 500 Internal Server Error.

The simplest approach: throw a custom exception from the service, and tell Spring which
HTTP status code it maps to.

---

## Step 1 — Create a custom exception

```java
package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
}
```

`@ResponseStatus` tells Spring: when this exception reaches the web layer, respond with 404.

---

## Step 2 — Throw it from the service

```java
@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
```

When `UserNotFoundException` is thrown, Spring automatically returns:

```
HTTP 404 Not Found
```

No extra configuration needed.

---

## Summary

| What | Why |
|---|---|
| Custom exception class | Gives the error a clear name |
| `@ResponseStatus` | Controls which HTTP status code is returned |
| `orElseThrow` | Clean way to throw when a value is missing |

---

## Note — Cross-Cutting Concerns

Exception handling is a classic example of a **cross-cutting concern**.

A cross-cutting concern is a piece of logic that applies across many parts of the application,
not just one specific class or layer. Other examples include logging, security, and transaction management.

Without a centralized handler, every controller or service would need to catch and format errors
individually — duplicating the same logic everywhere.

`@RestControllerAdvice` is Spring's answer to this: define the concern once, apply it everywhere.
Under the hood, it is built on **Spring AOP (Aspect-Oriented Programming)**, a mechanism
specifically designed for cross-cutting concerns.

We will not need to write AOP code directly in most projects.
But when we encounter it, this is the problem it solves.

---
