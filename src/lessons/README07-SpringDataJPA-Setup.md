
# JPA Repository with PostgreSQL

## 🧩 What is JPA?

**JPA (Java Persistence API)** is a specification that defines how Java objects (entities) map to database tables.

There are several layers involved:

| Layer | What it is |
|---|---|
| **JPA** | A specification (set of interfaces and rules) — it defines *what* should happen |
| **Hibernate** | The most common JPA *implementation* — it is the engine that actually talks to the database |
| **Spring Data JPA** | A Spring abstraction on top of Hibernate — removes boilerplate, provides repositories |

In a typical Spring Boot project we write Spring Data JPA code, Spring Data JPA delegates to Hibernate, and Hibernate translates everything into SQL.

---

## pom.xml Dependencies

To use Spring Data JPA with PostgreSQL, we need two dependencies in `pom.xml`:

```xml
<!-- Spring Data JPA (includes Hibernate) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- PostgreSQL JDBC driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

- `spring-boot-starter-data-jpa` pulls in Hibernate, the JPA API, and Spring Data JPA together.
- `postgresql` is the JDBC driver that lets the application connect to a PostgreSQL database.

> If we are using **Spring Initializr** (start.spring.io), we can search for  
> **"Spring Data JPA"** and **"PostgreSQL Driver"** and they will be added automatically.

---

## What is a JPA Repository?

A **JPA repository** is an interface that allows us to perform CRUD (Create, Read, Update, Delete) operations directly on our entities.

**Repository Hierarchy** (`JpaRepository` → `PagingAndSortingRepository` → `CrudRepository`):

- **`CrudRepository`** — Basic CRUD methods:
  - `save(entity)` → returns the saved entity (`T`)
  - `findById(id)` → returns `Optional<T>` (empty if not found)
  - `findAll()` → returns `Iterable<T>`
  - `delete(entity)` → returns `void`
  - `count()` → returns `long`
```java
public interface UserRepository extends CrudRepository<User, Long> {}
```

- **`PagingAndSortingRepository`** — Extends `CrudRepository` and adds:
  - `findAll(Pageable pageable)` → returns `Page<T>` (includes content + metadata like total pages)
  - `findAll(Sort sort)` → returns `Iterable<T>` sorted
```java
public interface UserRepository extends PagingAndSortingRepository<User, Long> {}
```

- **`JpaRepository`** — Most feature-rich. Includes everything above plus:
  - `findAll()` → returns `List<T>` (instead of `Iterable<T>`)
  - `saveAll(entities)` → returns `List<T>`
  - `flush()` → returns `void` (syncs pending changes to DB immediately)
  - `deleteInBatch(entities)` → returns `void` (single DELETE query, more efficient)
  - `getReferenceById(id)` → returns a lazy proxy `T` (throws exception only on access if not found)

  **This is the most commonly used one.**
```java
public interface UserRepository extends JpaRepository<User, Long> {}
```

---

## Database Configuration (PostgreSQL)

In our `application.properties` (or `application.yml`):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/mydatabase
spring.datasource.username=postgres
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

---

## Entity Example

```java
import jakarta.persistence.*;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    // Getters and Setters
}
```

---

## 🧰 Repository Example

```java
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // We can add custom query methods here
    User findByEmail(String email);
}
```

---

## 🚀 Common JPA Repository Methods

| Method                    | Description                                                      | Example                               |
|---------------------------|------------------------------------------------------------------|---------------------------------------|
| `findAll()`               | Returns all entities from the table.                             | `userRepository.findAll()`            |
| `findById(ID id)`         | Returns an entity by its ID (wrapped in `Optional`).             | `userRepository.findById(1L)`         |
| `save(Entity e)`          | Saves a new entity or updates an existing one.                   | `userRepository.save(new User(...))`  |
| `deleteById(ID id)`       | Deletes an entity by its ID.                                     | `userRepository.deleteById(1L)`       |
| `delete(Entity e)`        | Deletes a specific entity instance.                              | `userRepository.delete(user)`         |
| `count()`                 | Returns the total number of entities.                            | `userRepository.count()`              |
| `existsById(ID id)`       | Checks if an entity exists by ID.                                | `userRepository.existsById(1L)`       |
| `flush()`                 | Forces the persistence context to synchronize with the database. | `userRepository.flush()`              |
| `getReferenceById(ID id)` | Returns a reference to the entity (lazy loading).                | `userRepository.getReferenceById(1L)` |

---

## Custom Finder Methods (Foreshadow)

Spring Data JPA can automatically generate queries based on method names.

Examples:

```java
List<User> findByName(String name);
List<User> findByEmailContaining(String keyword);
List<User> findByNameAndEmail(String name, String email);
List<User> findByIdBetween(Long start, Long end);
```

---
