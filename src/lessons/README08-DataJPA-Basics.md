# README 7 – Spring Data JPA: Derived Methods, @Query, and Query by Example

This section covers how Spring Data JPA lets us query a database without writing most of the boilerplate ourselves.

Before we start, a common question:

---

## Is this Hibernate or Spring?

Good question. Let us be precise.

| Concept | Who owns it |
|---|---|
| `Session`, `SessionFactory` | Hibernate |
| `EntityManager`, `Persistence` | JPA (standard interface) |
| JPQL | JPA standard |
| HQL | Hibernate (superset of JPQL) |
| Criteria API | JPA standard |
| `JpaRepository`, derived methods | Spring Data JPA |
| `@Query` in a repository | Spring Data JPA |
| Query by Example (`Example.of`) | Spring Data JPA |

In other words:

- **Hibernate** is the JPA implementation — it does the actual work
- **JPA** is the standard interface on top of it
- **Spring Data JPA** is a layer on top of JPA that eliminates boilerplate and adds conventions like method name parsing

In a Spring Boot project, all three work together:

```
Our code
  ↓
Spring Data JPA  (repositories, derived methods, QBE)
  ↓
JPA (EntityManager, JPQL, Criteria)
  ↓
Hibernate (actual SQL generation, caching, session management)
  ↓
Database
```

So the derived method feature is **Spring-specific**.
HQL and Criteria are **not Spring**, and they work fine without Spring.

---

## Overview of approaches in this section

| Approach | What it is | Best for |
|---|---|---|
| Derived methods | method name → SQL | simple standard lookups |
| `@Query` | write HQL inside a Spring repo | complex queries |
| Query by Example (QBE) | probe object → SQL | optional-field search forms |

---

## Part 1 – Spring Data JPA – derived query methods

In Spring Boot, we do not usually write queries by hand at all for simple cases.

Spring Data JPA can generate queries automatically from **method names**.

This is called **derived queries** or **query derivation**.

### How it works

We define a repository interface:

```java
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByTitle(String title);
}
```

Spring reads the method name `findByTitle` and generates the following SQL automatically:

```sql
select * from books where title = ?
```

We did not write any query.
Spring parsed the method name and did it for us.

### More examples

```java
public interface BookRepository extends JpaRepository<Book, Long> {

    // exact match
    List<Book> findByTitle(String title);

    // year condition
    List<Book> findByYearGreaterThan(int year);

    // contains check (LIKE %...%)
    List<Book> findByTitleContaining(String keyword);

    // starts with (LIKE ...%)
    List<Book> findByTitleStartingWith(String prefix);

    // combined condition
    List<Book> findByTitleAndYear(String title, int year);

    // OR condition
    List<Book> findByTitleOrYear(String title, int year);

    // ordered
    List<Book> findByYearGreaterThanOrderByTitleAsc(int year);

    // top N
    List<Book> findTop3ByOrderByYearDesc();

    // first match only
    Optional<Book> findFirstByTitle(String title);

    // count
    long countByYear(int year);

    // existence check
    boolean existsByTitle(String title);

    // IN clause
    List<Book> findByIdIn(List<Long> ids);
}
```

### Keyword reference (most common)

| Keyword | SQL equivalent |
|---|---|
| `findBy` | `WHERE` |
| `And` | `AND` |
| `Or` | `OR` |
| `Is`, `Equals` | `= ?` |
| `Not` | `<>` |
| `LessThan` | `< ?` |
| `GreaterThan` | `> ?` |
| `Between` | `BETWEEN ? AND ?` |
| `Like` | `LIKE ?` |
| `Containing` | `LIKE %...%` |
| `StartingWith` | `LIKE ...%` |
| `EndingWith` | `LIKE %...` |
| `In` | `IN (...)` |
| `NotIn` | `NOT IN (...)` |
| `OrderBy` | `ORDER BY` |
| `Top`, `First` | `LIMIT` |
| `Distinct` | `SELECT DISTINCT` |

### How JpaRepository works

When we write:

```java
public interface BookRepository extends JpaRepository<Book, Long> { }
```

We get these methods for free without writing any implementation:

- `save(entity)` – insert or update
- `findById(id)` – find by primary key
- `findAll()` – select all
- `deleteById(id)` – delete by primary key
- `count()` – total row count
- `existsById(id)` – check if a record exists

Spring Boot creates the implementation automatically at startup.

---

## Part 2 – @Query

### When method names get too long

Sometimes the method name becomes unreadable:

```java
List<Book> findByYearGreaterThanAndTitleContainingOrderByYearDesc(int year, String title);
```

In that case, use `@Query` to write the HQL yourself:

```java
@Query("select b from Book b where b.year > :year and b.title like %:title% order by b.year desc")
List<Book> searchBooks(@Param("year") int year, @Param("title") String title);
```

This gives us a shorter, readable method name and a clear query.

### @Query with JOIN

We can write full HQL with joins inside a repository:

```java
@Query("select distinct b from Book b " +
       "join b.reviews r " +
       "where r.rating >= :minRating")
List<Book> findBooksWithHighRatings(@Param("minRating") int minRating);
```

### Native SQL with @Query

Sometimes we need actual SQL instead of HQL.

```java
@Query(value = "select * from books where year > :year", nativeQuery = true)
List<Book> findNewerThan(@Param("year") int year);
```

Use `nativeQuery = true` and write regular SQL.

Downsides:

- not portable across databases
- bypasses HQL's entity mapping abstraction

Use native queries only when we genuinely need SQL features that HQL cannot express.

---

## Part 3 – Query by Example (QBE)

Query by Example (QBE) is a technique where we create a **probe object** and use it as the search template.

Instead of writing a query, we say:

> "Find all books that look like this example object."

### Basic usage in Spring Data

```java
Book probe = new Book();
probe.setTitle("Spring");

Example<Book> example = Example.of(probe);

List<Book> result = bookRepository.findAll(example);
```

This generates something like:

```sql
select * from books where title = 'Spring'
```

Fields that are `null` in the probe are **ignored**.
Only non-null fields are included in the query.

So if we only set `title`, the query filters only by title.

### ExampleMatcher – controlling matching behavior

By default, matching is exact.
`ExampleMatcher` gives us more control.

```java
ExampleMatcher matcher = ExampleMatcher.matching()
        .withMatcher("title", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase())
        .withIgnorePaths("year");

Book probe = new Book();
probe.setTitle("spring");

Example<Book> example = Example.of(probe, matcher);

List<Book> result = bookRepository.findAll(example);
```

What this does:

- match `title` with `LIKE %spring%` and ignore case
- ignore `year` even if it has a value

Common matcher options:

| Method | Behavior |
|---|---|
| `exact()` | exact match (default) |
| `contains()` | LIKE `%value%` |
| `startsWith()` | LIKE `value%` |
| `endsWith()` | LIKE `%value` |
| `ignoreCase()` | case-insensitive |

### When is QBE useful?

- search forms where users fill in a few optional fields
- filtering lists without writing conditional HQL or Criteria
- quick prototyping

### Limitations of QBE

- only works on simple field types (not joins or nested entities)
- does not support range conditions like `age > 20`
- does not support ordering or aggregation

---

## Part 4 – @Transactional

### What is a transaction?

A transaction is a group of database operations that either **all succeed** or **all fail together**.

If any step fails, the whole group is rolled back — as if nothing happened.

Classic example: transfer money between two accounts.

```
1. Subtract 100 from account A
2. Add 100 to account B
```

If step 1 succeeds but step 2 throws an exception, we have lost 100 euros.
A transaction prevents this: both operations are wrapped together, and if anything fails, both are undone.

---

### How to use it

Add `@Transactional` to a service method:

```java
@Service
public class BankService {

    private final AccountRepository accountRepository;

    public BankService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void transfer(Long fromId, Long toId, int amount) {
        Account from = accountRepository.findById(fromId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        Account to = accountRepository.findById(toId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);

        accountRepository.save(from);
        accountRepository.save(to);
        // if an exception is thrown here, both saves are rolled back
    }
}
```

If a `RuntimeException` is thrown anywhere inside this method, Spring rolls back all database changes made in that method call.

---

### Where to place it

`@Transactional` goes on the **service layer**, not the controller or repository.

The repository methods (`save`, `findById`, etc.) are already transactional individually by default.
The service is where we coordinate multiple operations that must succeed or fail together.

```
Controller      →  handles HTTP, calls service
Service         →  @Transactional, coordinates operations
Repository      →  single DB operations, already wrapped
```

---

### Default rollback behavior

By default, Spring rolls back on **unchecked exceptions** (`RuntimeException` and its subclasses).

It does **not** roll back on checked exceptions unless we tell it to.

```java
// rolls back on RuntimeException (default)
@Transactional
public void doSomething() { ... }

// rolls back on any exception, including checked
@Transactional(rollbackFor = Exception.class)
public void doSomethingStrict() { ... }

// never rolls back on a specific exception
@Transactional(noRollbackFor = IllegalArgumentException.class)
public void doSomethingSelective() { ... }
```

---

### Read-only transactions

For methods that only read data, we can mark them as read-only.
This is a hint to the database that no writes will happen, which can improve performance.

```java
@Transactional(readOnly = true)
public List<Book> getAllBooks() {
    return bookRepository.findAll();
}
```

---

### How it works under the hood

`@Transactional` is implemented using **AOP**.

When Spring sees `@Transactional` on a method, it wraps the method call with `@Around` advice that:
1. opens a database transaction before the method runs
2. commits it if the method returns successfully
3. rolls it back if an exception is thrown

We do not write any of this — Spring does it for us through the same AOP mechanism described in the cross-cutting concerns notes.

---

### Summary

| Concept | Meaning |
|---|---|
| Transaction | A group of DB operations that succeed or fail together |
| `@Transactional` | Wraps the method in a transaction automatically |
| Rollback | Undo all changes if something goes wrong |
| `readOnly = true` | Optimization hint for read-only methods |
| Where to use | Service layer, on methods that coordinate multiple DB operations |

---

## Comparison overview

| Approach | Best for | Requires Spring? |
|---|---|---|
| HQL (README 5) | fixed named queries, joins, aggregation | no |
| Criteria API (README 5) | dynamic filters at runtime | no |
| Spring Data derived methods | simple standard lookups | yes |
| `@Query` | complex queries inside a Spring repo | yes |
| Query by Example | optional-field search forms | yes |

---

## Common mistakes

### Making derived method names too long

Long derived method names are hard to read and maintain.
When the name gets unwieldy, switch to `@Query`.

### Using QBE for range filters

QBE cannot express `age > 20`.
Use Criteria or `@Query` for range conditions.

### Using native queries without a reason

Native SQL ties our code to a specific database.
Use HQL or JPQL first. Only fall back to native queries when there is a genuine need.

### Not checking generated SQL

Derived methods and QBE generate SQL automatically.
Always verify that the generated SQL:

- is doing what we expect
- is not loading more data than needed

Always keep SQL logs on during development.

---

## Summary

- **Spring Data JPA** is a layer on top of JPA/Hibernate that reduces boilerplate
- **Derived methods**: method name → query generated automatically; zero query writing for simple lookups
- **`@Query`**: write HQL (or native SQL) inside a Spring repo when derived names become too complex
- **Query by Example**: use a probe object as a search template; good for optional-field search forms
- **HQL and Criteria** do not require Spring and continue to work in plain Hibernate as well
