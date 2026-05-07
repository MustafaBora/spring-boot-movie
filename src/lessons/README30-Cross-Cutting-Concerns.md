# Cross-Cutting Concerns and AOP

## What is a cross-cutting concern?

Most of the logic in your application belongs to a specific layer.
User validation belongs in the service. Database access belongs in the repository. HTTP handling belongs in the controller.

Some logic, however, does not belong to any one layer — it needs to run across all of them.

Examples:
- **Logging** — record what methods were called, with what arguments, how long they took
- **Security** — check if the caller is authenticated before any method runs
- **Transaction management** — wrap database operations in a transaction automatically
- **Exception handling** — intercept errors from any layer and format them consistently

This kind of logic is called a **cross-cutting concern**.

The problem: if you write this logic directly in each class, you repeat it everywhere.
The solution: write it once and apply it across the whole application.

---

## Real-life analogy

Business logic = 
* cooking
* cutting the grass
* painting the house
* fixing the car

Cross-cutting = electricity

You don’t rewrite electricity in every room of the house. 
You just plug in your appliances and it works everywhere. 

---


## DRY Principle (Don't Repeat Yourself)

A key reason cross-cutting concerns exist is the **DRY principle**.

**DRY = Don't Repeat Yourself**

Avoid duplicating the same logic in multiple places.

---

### Without DRY (Bad)

Imagine handling errors manually in every controller:

```java
@GetMapping("/{id}")
public ResponseEntity<?> getById(Long id) {
    try {
        //log it give the pattern what to log
        //ask if the user is authenticated
        return ResponseEntity.ok(service.getById(id));
    } catch (Exception e) {
        return ResponseEntity.status(404).body("Not found");
    }
}
```
You would repeat this logic in:

* every controller
* every endpoint
* every method that calls the service, repository and database

This leads to:

- duplicated code 
- inconsistent behavior
- hard maintenance
- more bugs
- slower development
- frustrated developers
- unhappy users
- lower productivity
- higher costs
- 
---

# AOP — Aspect-Oriented Programming

**AOP** is a programming paradigm that lets you separate cross-cutting concerns from business logic.

Instead of writing logging code inside every method, you define it once in an **Aspect** and tell Spring where to apply it.

### Core vocabulary

| Term | Meaning |
|---|---|
| **Aspect** | The class that contains the cross-cutting logic |
| **Advice** | The method inside the Aspect that runs (the actual logic) |
| **Pointcut** | An expression that defines *where* the Advice applies |
| **Join point** | A specific moment in execution where Advice **can be** inserted (usually a method call) |

For a visual image explaining these terms: https://stackoverflow.com/questions/15447397/spring-aop-whats-the-difference-between-joinpoint-and-pointcut

---

## Advice types

| Type | When it runs |
|---|---|
| `@Before` | Before the target method |
| `@After` | After the target method (always, even if exception thrown) |
| `@AfterReturning` | After the target method returns successfully |
| `@AfterThrowing` | After the target method throws an exception |
| `@Around` | Before and after — you control execution completely |

---

## Library we need to add
Add the dependency to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

## Example — Logging with @Around

The string passed to `@Around("...")` is the **Pointcut expression** — it selects which methods to intercept. Read `execution(* com.example.demo.service.*.*(..))` as "all methods in all classes in the `service` package, with any parameters". The full Pointcut syntax is explained later in this file.

`ProceedingJoinPoint` is the object Spring passes to `@Around` advice — it represents the intercepted method call:
- `.getSignature().getName()` — returns the method's name
- `.proceed()` — executes the original method and returns its result

Create the Aspect class:

```java
package com.example.demo.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    // This method will run around (before and after) any method in any service class
    // You can change the pointcut expression to target specific methods or packages
    // The ".." means "any parameters"
    // The syntax is flexible and powerful — see the end of this file for more examples
    @Around("execution(* com.example.demo.service.*.*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("Calling: " + methodName);

        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed(); // run the actual method
        long duration = System.currentTimeMillis() - start;

        System.out.println("Finished: " + methodName + " (" + duration + "ms)");
        return result;
    }
}
```

Every method in any service class will now be logged automatically.
No changes needed in the service classes themselves.

---

## Example — @Before for a simpler case

```java
@Aspect
@Component
public class AuditAspect {

    @Before("execution(* com.example.demo.service.UserService.delete*(..))")
    public void beforeDelete() {
        System.out.println("Delete operation triggered");
    }
}
```

This runs before any method in `UserService` whose name starts with `delete`.

---

## Pointcut expression syntax

```
execution(modifiers? returnType declaringType? methodName(params) throws?)
```

Examples:

| Expression | Matches |
|---|---|
| `execution(* com.example.service.*.*(..))` | All methods in all service classes |
| `execution(* com.example.service.UserService.*(..))` | All methods in `UserService` |
| `execution(* com.example.service.UserService.find*(..))` | Methods starting with `find` |
| `execution(public * *(..))` | All public methods anywhere |
| `execution(* com.example..*.*(..))` | All methods in any class in `com.example` or its subpackages |
| `execution(* *..*Controller.*(..))` | All methods in any class whose name ends with `Controller` |
| `execution(* com.example.service.*.save*(..))` | Methods starting with `save` in any class in the service package |

---

## Spring's built-in AOP annotations

We have been writing Aspects manually so far. But Spring already ships with several ready-made Aspects behind the scenes. We just use the annotation — Spring wires up the proxy automatically.

The three most important ones are:

| Annotation | What it does |
|---|---|
| `@Transactional` | Wraps the method in a database transaction |
| `@Cacheable` | Caches the return value so the method is skipped on repeat calls |
| `@Async` | Runs the method in a background thread |

We do not need to write Aspects for these — Spring provides them.
But knowing the mechanism helps us understand why they work the way they do,
and prepares us to write our own when the need arises.

---

### `@Transactional` — the one we have already seen

We used `@Transactional` earlier when learning JPA. Now we can see why it exists at all.

When a service method does multiple database operations, we want them to succeed or fail together — **all or nothing**. If the second save fails, the first one should be rolled back automatically.

Without `@Transactional` we would have to write this boilerplate in every method:

```java
// Without AOP — manual transaction management
public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
    EntityTransaction tx = entityManager.getTransaction();
    try {
        tx.begin();
        accountRepository.debit(fromId, amount);
        accountRepository.credit(toId, amount);
        tx.commit();
    } catch (Exception e) {
        tx.rollback();
        throw e;
    }
}
```

With `@Transactional`, Spring's built-in Aspect wraps the method for us:

```java
// With AOP — clean business logic, transaction handled by Spring
@Transactional
public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
    accountRepository.debit(fromId, amount);
    accountRepository.credit(toId, amount);
    // Spring commits on success, rolls back on any RuntimeException
}
```

This is exactly what an `@Around` Aspect does — it runs code before and after our method, and we never have to touch our business logic.

> **Why is this AOP?**  
> `@Transactional` does not change what our method does. It wraps it from the outside.  
> Spring creates a **proxy** around the class, intercepts every call to an annotated method,  
> opens a transaction before, and commits (or rolls back) after.  
> The business logic stays clean. The transaction concern lives in one place.

---

### `@Cacheable` and `@Async` — brief mention

`@Cacheable` follows the same idea. Instead of reaching the database every time, Spring checks a cache first:

```java
@Cacheable("products")
public Product findById(Long id) {
    // Spring intercepts this call
    // On the first call: runs the method, stores the result in cache
    // On repeat calls with the same id: returns from cache, skips the method entirely
    return productRepository.findById(id).orElseThrow();
}
```

`@Async` moves a method call to a background thread — useful for sending emails or notifications without making the user wait:

```java
@Async
public void sendWelcomeEmail(String email) {
    // Spring intercepts this call and runs it in a separate thread
    emailClient.send(email, "Welcome!");
}
```

Both work through the same proxy mechanism as `@Transactional`. We add an annotation, Spring wraps the method with an Aspect, and our code stays focused on what it does — not how it is executed.

---

## Summary

| Concept | Meaning |
|---|---|
| Cross-cutting concern | Logic that spans multiple layers (logging, security, transactions) |
| Aspect | Class that holds the cross-cutting logic |
| Advice | The method that runs at the join point |
| Pointcut | Expression that selects where the Advice applies |
| `@Around` | Most flexible Advice — runs before and after, can modify result |
| `@Transactional` | Example of AOP in Spring Boot for managing transactions |
| `@RestControllerAdvice` | Example of AOP for handling exceptions globally |
| `execution(* com.example..*.*(..))` | Common Pointcut expression to match all methods in a package and subpackages |

AOP lets you keep business logic clean.
Controllers handle HTTP. Services handle business rules. Aspects handle the rest.

---
