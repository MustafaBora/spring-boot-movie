# 📘 Spring Data JPA — Derived Query Methods Guide

Spring Data JPA can auto-generate SQL queries **based on method names**.
We don't write SQL — instead, we follow specific naming patterns.

> ✨ If we follow the **right keywords + entity field names**, Spring implements the query for us!

---

## * Basic Structure

```java
findBy + FieldName + Condition
readBy + FieldName + Condition
getBy + FieldName + Condition
```

**Examples:**

```java
findByTitle(String title)
findByAuthor(String author)
```

---

## Supported Keywords

### 1️⃣ **Equality & Comparison**

| Keyword                | Meaning   | Example                           |
|------------------------|-----------|-----------------------------------|
| `findBy`               | Equals    | `findByTitle(String title)`       |
| `findBy...Not`         | Not equal | `findByAuthorNot(String author)`  |
| `findBy...LessThan`    | `<`       | `findByYearLessThan(int year)`    |
| `findBy...GreaterThan` | `>`       | `findByYearGreaterThan(int year)` |
| `findBy...Between`     | Range     | `findByYearBetween(1990, 2020)`   |

---

### 2️⃣ **String Matching**

| Keyword      | Meaning          | Example                          |
|--------------|------------------|----------------------------------|
| `Containing` | LIKE %val%       | `findByTitleContaining("code")`  |
| `StartsWith` | LIKE val%        | `findByAuthorStartsWith("Rob")`  |
| `EndsWith`   | LIKE %val        | `findByAuthorEndsWith("Martin")` |
| `IgnoreCase` | Case-insensitive | `findByGenreIgnoreCase("Drama")` |

---

### 3️⃣ **Boolean Operators**

| Keyword | Example                                             |
|---------|-----------------------------------------------------|
| `And`   | `findByAuthorAndGenre(String author, String genre)` |
| `Or`    | `findByTitleOrAuthor(String title, String author)`  |

---

### 4️⃣ **Sorting**

| Keyword              | Example                                      |
|----------------------|----------------------------------------------|
| `OrderBy<Field>Asc`  | `findByAuthorOrderByYearAsc(String author)`  |
| `OrderBy<Field>Desc` | `findByAuthorOrderByYearDesc(String author)` |

---

### 5️⃣ **Null / Not Null**

| Keyword     | Example                  |
|-------------|--------------------------|
| `IsNull`    | `findByGenreIsNull()`    |
| `IsNotNull` | `findByGenreIsNotNull()` |

---

### 6️⃣ **Check Existence**

| Keyword    | Example                       |
|------------|-------------------------------|
| `existsBy` | `existsByTitle(String title)` |

---

## IMPORTANT RULES

### * Field names must match Entity fields

Entity:

```java
private String author;
```

Method must match casing:

```java
findByAuthor() ✅
findByauthor() ❌
findByWriter() ❌ (no such field)
```

---

### * We can chain keywords

```java
findByAuthorIgnoreCaseAndGenreIgnoreCase(String a, String g)
```

---

### * Use `List<...>` or Optional return types

```java
List<Book> findByGenre(String genre);
Optional<Book> findByTitle(String title);
```

---

## 🚫 When JPA WON’T Implement the Method

X Wrong field name
X Wrong keyword order
X Complex logic (use `@Query` instead)

Example (invalid):

```java
findBookBySomethingFancyThatDoesNotExist()
```

---

## If method name gets too big — use `@Query`

```java
@Query("SELECT b FROM Book b WHERE LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))")
List<Book> searchAuthor(@Param("author") String author);
```

---

## 🎬 Example Repository

```java
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByAuthorContainingIgnoreCase(String author);
    List<Book> findByGenreIgnoreCase(String genre);

    List<Book> findByYearGreaterThan(int year);
    List<Book> findByYearBetween(int start, int end);

    boolean existsByTitle(String title);

    List<Book> findAllByOrderByYearAsc();
    List<Book> findAllByOrderByYearDesc();
}
```

---

## Summary

Spring Data JPA derived query methods let you query the database just by writing a method name — no SQL required.

- **Simple queries** (find by one field, check existence) → just write the method name and Spring generates the query for you
- **Filtering + sorting** → combine `findBy` with `OrderBy` in the same method name
- **String searches** → use `Containing`, `StartsWith`, `EndsWith`, `IgnoreCase` for flexible matching
- **Complex queries** → when the method name gets too long or the logic is too advanced, switch to `@Query` and write JPQL directly

The most important rule: **field names in the method must exactly match field names in your entity class** (same spelling, same capitalisation).

---
