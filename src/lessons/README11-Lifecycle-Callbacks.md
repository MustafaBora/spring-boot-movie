# 📅 JPA Lifecycle Callback Annotations

JPA entities go through a well-defined **lifecycle**: they are created, updated, and eventually deleted.  
**Lifecycle callback annotations** let us run our own logic automatically at specific points in that lifecycle — without changing any repository or service code.

---

## What is the Persistence Context?

Before we look at the lifecycle, we need to understand what is managing it.

The **Persistence Context** is a short-lived, in-memory workspace that JPA maintains during a transaction.  
It sits between our application code and the database.

```
[ Our Code ]  ←→  [ Persistence Context ]  ←→  [ Database ]
```

- When we load an entity (e.g. `movieRepository.findById(1L)`), JPA puts a copy of it into the Persistence Context.
- While the entity lives there it is called **managed** — JPA watches it for changes.
- When the transaction ends, JPA **flushes** all changes to the database automatically.
- After the transaction ends, the Persistence Context is closed and entities become **detached**.

In a Spring Boot application, the Persistence Context is created automatically at the start of a `@Transactional` method and closed when it returns.

---

## The Entity Lifecycle

```
New  ──[persist]──→  Managed  ──[remove]──→  Removed
                      ↓    ↑
                 [detach]  [merge]
                      ↓    |
                    Detached
```

- **persist** — saves a new entity to the database for the first time (`INSERT`)
- **detach** — when the transaction ends, JPA automatically releases the entity; it becomes **Detached** (JPA no longer tracks it)
- **merge** — takes a Detached entity back into the Managed state and writes its changes to the database (`UPDATE`)
- **remove** — marks the entity for deletion (`DELETE`)

> In Spring Boot, `repository.save(entity)` handles this automatically:
> if the entity has no `id` it calls **persist**; if it already has an `id` it calls **merge**.

| State       | Meaning                                                     |
|-------------|-------------------------------------------------------------|
| **New**     | Object created in Java, not yet known to the database       |
| **Managed** | Tracked by JPA — changes are automatically synced           |
| **Detached**| Was managed, now disconnected from the Persistence Context  |
| **Removed** | Marked for deletion — will be deleted on the next flush     |

---

## Available Callback Annotations

| Annotation      | Fires…                                              |
|-----------------|-----------------------------------------------------|
| `@PrePersist`   | **before** the entity is first inserted into the DB |
| `@PostPersist`  | after the entity has been inserted                  |
| `@PreUpdate`    | before an existing entity is updated                |
| `@PostUpdate`   | after an existing entity has been updated           |
| `@PreRemove`    | before the entity is deleted                        |
| `@PostRemove`   | **after** the entity has been deleted               |
| `@PostLoad`     | after the entity has been loaded from the DB        |

---

## 1) `@PrePersist` — Run logic before saving a new entity

A very common use case is **automatically setting timestamps**.

### Example — auto-set `createdAt` on first save

```java
@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDateTime createdAt;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
```

- The `onPrePersist()` method is called **automatically** by JPA just before the `INSERT` SQL runs.
- We never call this method ourselves — JPA calls it for us.
- No changes to the repository or service are needed.

### Other common `@PrePersist` uses

| Use case                         | What to do inside the method              |
|----------------------------------|-------------------------------------------|
| Set `createdAt` timestamp        | `this.createdAt = LocalDateTime.now()`    |
| Set a default status             | `this.status = "ACTIVE"`                  |
| Normalise data (e.g. trim/lowercase) | `this.email = email.trim().toLowerCase()` |
| Generate a UUID reference number | `this.ref = UUID.randomUUID().toString()` |

---

## 2) `@PostRemove` — Run logic after an entity is deleted

`@PostRemove` fires **after** the `DELETE` SQL has been executed and the transaction has committed that operation.

### What belongs here — and what does not

Entities are **data holders**. In the standard Spring layered architecture (Controller → Service → Repository), business logic belongs in the **Service layer**, not in the entity.

| Acceptable inside an entity callback | Belongs in the Service layer instead |
|--------------------------------------|--------------------------------------|
| Setting `createdAt` / `updatedAt`    | Calling another repository           |
| Normalising field values             | Publishing application events        |
| Generating a UUID slug               | Sending emails or notifications      |
| Logging a simple diagnostic message  | Any logic that needs a Spring bean   |

Spring beans (repositories, services, event publishers) **cannot be injected into an entity** with `@Autowired` — JPA creates entities itself, outside the Spring container.

### Example — logging with SLF4J

Logging is fine in a callback because it does not require Spring injection.  
In production code we use **SLF4J** (the standard logging API in Spring Boot) instead of `System.out`.

> `LoggerFactory.getLogger(...)` is a **static factory method call** — not Spring injection.  
> It runs once when the class is loaded and has no dependency on the Spring context.
> **Note** This is still logic inside the entity, but it is not business workflow logic. It is limited to lightweight lifecycle diagnostics. Business actions such as audit writes, notifications, or event publishing should stay in the service layer.

```java
@Entity
public class Movie {

    // Static field — initialised by LoggerFactory, not by Spring
    private static final Logger log = LoggerFactory.getLogger(Movie.class);
    // With Lombok you can replace the two lines above with just: @Slf4j

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @PostRemove
    public void onPostRemove() {
        // Acceptable: a lightweight diagnostic log
        log.warn("Movie deleted — id={}, title='{}'", this.id, this.title);
    }
}
```

If we need to write an audit row or publish an event after a deletion, we do it in the **Service layer**:

```java
// MovieService.java — the right place for business logic
@Transactional
public void deleteMovie(Long id) {
    Movie movie = movieRepository.findById(id).orElseThrow();
    movieRepository.delete(movie);          // triggers @PostRemove log inside the entity
    auditLogRepository.save(              // business logic stays here, in the service
        new AuditLog("MOVIE_DELETED", id)
    );
    eventPublisher.publishEvent(new MovieDeletedEvent(id, movie.getTitle()));
}
```

### Difference between `@PreRemove` and `@PostRemove`

| Annotation     | Fires          | Entity state        | Typical use                                      |
|----------------|----------------|---------------------|--------------------------------------------------|
| `@PreRemove`   | before DELETE  | still in DB         | Detach relationships, business validation        |
| `@PostRemove`  | after DELETE   | gone from DB        | Audit logging, sending events, cleanup side effects |

---

## Putting it all together — a full entity example

```java
@Entity
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Called automatically before first INSERT
    @PrePersist
    public void onPrePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Called automatically before each UPDATE
    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Called automatically after DELETE — keep this lightweight (logging only)
    @PostRemove
    public void onPostRemove() {
        log.warn("Movie deleted — id={}, title='{}'", this.id, this.title);
        // For audit rows or events, do that in MovieService.deleteMovie() instead
    }
}
```

---

## Key rules

* Callback methods must be **`void`** and take **no parameters**.
* They must **not** throw checked exceptions.
* They can be **`private`**, `protected`, or `public` — JPA will still call them.
* They live **inside the entity class** (or in a separate `@EntityListeners` class for reuse across entities).

---

## Summary

| Annotation    | When it fires        | Most common use                           |
|---------------|----------------------|-------------------------------------------|
| `@PrePersist` | Before first INSERT  | Set `createdAt`, defaults, normalise data |
| `@PostRemove` | After DELETE         | Lightweight diagnostic log                |

> Business logic that needs a Spring bean (audit tables, events, emails) belongs in the **Service layer**, not in the entity.
