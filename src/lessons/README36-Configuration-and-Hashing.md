# Spring Boot Configuration: `@SpringBootApplication`, `@Configuration` & `@Bean`

In this section we look at how Spring Boot's configuration system works — how we declare beans, how Spring wires them together, and what `@SpringBootApplication` actually does under the hood. We use **BCrypt password hashing** as our running example throughout, because it is a real security requirement that shows exactly why `@Configuration` and `@Bean` matter in practice.

---

## Why Hash Passwords and BCrypt? (Our Running Example)

Before diving into configuration, let us understand what we are trying to configure.

### Why Hash Passwords?
Storing plain-text passwords is dangerous. If your database is leaked, all user accounts are exposed.

Instead, we store a **hashed** version of the password — this process is **one-way**, meaning we can **verify** a password without ever needing to reverse the hash.

BCrypt:
- Automatically generates a **salt** (extra protection)
- Designed to be **slow** on purpose to resist brute-force attacks

---

## Dependency

`BCryptPasswordEncoder` is part of Spring Security — no extra library needed beyond:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

---

## `@SpringBootApplication`

Every Spring Boot project starts with a class annotated `@SpringBootApplication`. That single annotation is actually a shortcut — it bundles three annotations together:

| Annotation | What it does |
|---|---|
| `@SpringBootConfiguration` | Marks the class as a source of bean definitions (extends `@Configuration`) |
| `@EnableAutoConfiguration` | Lets Spring Boot automatically configure beans based on the dependencies on the classpath |
| `@ComponentScan` | Tells Spring to scan the current package (and sub-packages) for components, services, repositories, etc. |

---

## What is `@Configuration`?

`@SpringBootConfiguration` is itself built on top of `@Configuration`, so we need to understand what `@Configuration` means.

Spring's **ApplicationContext** is the container that holds and manages all the beans in our application — think of it as the central registry Spring uses to wire everything together.

A class annotated with `@Configuration` is a **bean definition class** — a place where we declare which objects Spring should create and manage. Methods inside it are annotated with `@Bean`, and Spring calls those methods once and stores the result in the ApplicationContext so it can inject them wherever needed.

```java
@Configuration
public class AppConfig {

    @Bean
    public SomeService someService() {
        return new SomeService();  // Spring manages this object
    }
}
```

We use `@Configuration` when we need to register a bean that is not a simple component (not a `@Service`, `@Repository`, or `@Controller`). Security configuration is a common example of this — we will see it in the next section.

---

## Using BCrypt in Spring Boot

In Spring Boot, define the encoder as a `@Bean` so it can be injected wherever needed:

```java
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {  // ✅ return the abstraction, not the concrete class
        return new BCryptPasswordEncoder();
    }
}
```

Inject and use it in your service:

```java
@Service
public class UserService {

    private final BCryptPasswordEncoder encoder;  // ⚠️ see the SOLID note below

    public UserService(BCryptPasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public void register(String rawPassword) {
        String hashedPassword = encoder.encode(rawPassword);
        // save hashedPassword to the database — never the raw password
    }

    public boolean login(String rawPassword, String storedHash) {
        return encoder.matches(rawPassword, storedHash);
    }
}
```

> **Why `encode()` and not `hash()`?**  
> We might expect the method to be called `hash()` — because BCrypt is a one-way function, not a reversible encoding like Base64.  
> Spring named it `encode()` because `BCryptPasswordEncoder` implements the `PasswordEncoder` interface, which is designed to be algorithm-agnostic. The word "encode" was chosen as a neutral term that works for BCrypt, Argon2, or anything else.  
> In practice, **`encode()` here is one-way**. There is no `decode()`. Do not let the name mislead you.  
> This naming choice is actually a Liskov Substitution Principle concern — see the SOLID section below.

---

## A SOLID Detour: DIP and ISP in Practice

Looking at the `UserService` above, we declared:

```java
private final BCryptPasswordEncoder encoder;
```

This works — but it violates two SOLID principles. Let us use this real example to understand them.

---

### Quick note: DI vs DIP — principle or pattern?

Before going further, two abbreviations that are easy to confuse:

| Abbreviation | Full name | What it is |
|---|---|---|
| **DIP** | Dependency Inversion **Principle** | A **design principle** (one of the SOLID five). States that modules should depend on abstractions, not concretions. |
| **DI** | Dependency **Injection** | A **design pattern** that implements DIP in practice. Spring's `@Autowired` / constructor injection is an example. |

DIP tells you *what* to aim for. DI is one *way* to get there.

---

### D — Dependency Inversion Principle (the "D" in SOLID)

> *High-level modules should not depend on low-level modules. Both should depend on abstractions.*

"High-level" and "low-level" are **relative** terms — they are not about whether a class is an interface or a concrete class. They are about what the class *knows*:

- `UserService` knows **business rules**: "when a user registers, hash the password and save it"
- `BCryptPasswordEncoder` knows **technical details**: how BCrypt's algorithm works internally

`UserService` is higher-level *relative to* `BCryptPasswordEncoder`. DIP says: the higher one should not reach down and grab the concrete implementation. It should talk to an abstraction.

**The violation:**

```java
private final BCryptPasswordEncoder encoder;  // XX depends on a concrete class
```

If we ever want to switch to Argon2, we have to open `UserService` and change it — a class that should have nothing to do with which hashing algorithm we choose.

**The fix:**

```java
private final PasswordEncoder encoder;  // ✅ depends on the abstraction
```

`UserService` now only knows that *something* can encode and match passwords. It does not care whether that something is BCrypt, Argon2, or anything else. The concrete choice lives in the `@Bean` method in `SecurityConfig` — which is exactly the right place for a technical decision.

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();  // swap this line to change algorithm — nothing else changes
}
```

---

### (Optional & Advanced) I — Interface Segregation Principle (the "I" in SOLID)

> *No client should be forced to depend on methods it does not use.*

`PasswordEncoder` is a small, focused interface:

```java
public interface PasswordEncoder {
    String encode(CharSequence rawPassword);
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
```

`UserService` only needs those two methods. By depending on `PasswordEncoder` instead of `BCryptPasswordEncoder`, we are also respecting ISP: we are binding `UserService` to the **smallest possible surface** — only the methods it actually uses.

If `BCryptPasswordEncoder` gained a dozen BCrypt-specific methods tomorrow, `UserService` would not even see them. That is ISP working as intended.

---

### (Optional & Advanced) L — Liskov Substitution Principle (the "L" in SOLID)

> *Subtypes must be substitutable for their base types without altering the correctness of the program.*

In simpler terms: if an interface makes a promise, every implementation must keep that promise — not just in method signatures, but in **meaning and behaviour**.

This is where Spring's own naming causes a problem. The `PasswordEncoder` interface says:

```java
String encode(CharSequence rawPassword);
```

The word "encode" implies a reversible transformation — Base64 encodes, URL encoding encodes. The mental model a reader builds is: *"if I can encode, I can decode"*.

But `BCryptPasswordEncoder` breaks that expectation: `encode()` is **not reversible**. There is no `decode()`. BCrypt is a one-way hash, not an encoding.

Someone could write a perfectly legal implementation that *is* reversible:

```java
public class Base64PasswordEncoder implements PasswordEncoder {
    public String encode(CharSequence raw) { return Base64.encode(raw); }  // ❌ reversible!
    public boolean matches(CharSequence raw, String stored) { ... }
}
```

This compiles, injects, and runs — but silently destroys your security guarantees. LSP is violated not in syntax, but in **semantic contract**: the interface name implies something the implementations are not required to honour.

The design that would have avoided this:

```java
public interface PasswordHasher {
    String hash(CharSequence rawPassword);       // name implies one-way
    boolean matches(CharSequence raw, String storedHash);
}
```

`BCryptPasswordHasher implements PasswordHasher` — no ambiguity, no LSP tension. Spring did not make this choice; it accepted the naming ambiguity in favour of a single generic abstraction across all algorithms. The note in the Spring docs that says "encode here is one-way" is an admission of this gap.

---

### Summary

| Principle | Where it appears | The lesson |
|---|---|---|
| **DIP** | `UserService` depending on `BCryptPasswordEncoder` directly | Depend on `PasswordEncoder` interface — the abstraction, not the concretion |
| **ISP** | Choosing which type to depend on | `PasswordEncoder` exposes only the two methods `UserService` actually needs |
| **LSP** | `PasswordEncoder` naming `encode()` | An interface's name and method names are part of its contract — implementations must honour the implied behaviour, not just the signature |

---

## Important rule

Never do:

password.equals(storedPassword)

Always do:

encoder.matches(raw, hash)

Because every `encode()` call produces a *different* hash (due to the random salt), doing `rawPassword.equals(storedHash)` will always return `false`, even when the password is correct. `encoder.matches(raw, hash)` knows how to extract the salt from the stored hash and re-hash the input the same way, so it can tell whether they match.

---


### Key Points

* Each `encode()` call produces a **unique** hash even for the same password — this is the salt at work (see README28)
* Always store the **hashed** password, never the plain one
* Use `.matches()` during login — never compare strings directly (see the rule above)

---



