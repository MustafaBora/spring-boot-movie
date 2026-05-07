# * Spring Boot Starter Validation

## Goal

Learn how to use **Spring Boot Starter Validation** to validate user input automatically using annotations like:

- `@NotBlank`
- `@NotNull`
- `@Email`
- `@Min`
- `@Max`
- `@Size`

---

## ⚙️ Dependency Setup

In your `pom.xml` (Maven project):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

This brings in the **Jakarta Bean Validation API** (previously `javax.validation`) and its default implementation (`Hibernate Validator`). We can then use validation annotations in your model classes, and Spring will automatically validate incoming data in your controllers.

---

## What This Dependency Does

It allows us to:

1. Add validation annotations to your **model fields**.
2. Use `@Valid` in your **controller methods** to automatically check inputs.
3. Return helpful error responses if data doesn’t meet your validation rules.

---

## Example Model (Book.java)

```java
package com.example.booklibrary.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @Min(value = 1000, message = "Year must be realistic")
    private int year;

    @NotBlank(message = "Genre is required")
    private String genre;

    // Getters and setters...
}
```

Each annotation checks a specific condition when a request comes in.

---

## Common Validation Annotations

| Annotation               | Applies To          | Description                       |
|--------------------------|---------------------|-----------------------------------|
| `@NotNull`               | Any type            | Must not be null                  |
| `@NotBlank`              | String              | Must not be empty or whitespace   |
| `@NotEmpty`              | String, Collections | Must not be null or empty         |
| `@Size(min, max)`        | String, Collection  | Checks length or size             |
| `@Email`                 | String              | Must be a valid email address     |
| `@Min(value)`            | Number              | Must be greater or equal to value |
| `@Max(value)`            | Number              | Must be less or equal to value    |
| `@Positive`              | Number              | Must be > 0                       |
| `@Pattern(regexp="...")` | String              | Must match regex pattern          |
| `@Past`                 | Date                | Must be a past date               |
| `@Future`               | Date                | Must be a future date             |
| `@Valid`                 | Any type            | Triggers validation on nested objects |
| `@AssertTrue`            | boolean             | Must be true                      |
| `@Digits(integer, fraction)` | Number              | Checks number of integer and fraction digits |

---
Not only entity fields but also method parameters can be validated using these annotations.
And also DTOs (Data Transfer Objects) can be used to separate validation logic from the entity model.
And custom validation annotations can be created for complex validation rules.
And validation groups can be used to apply different validation rules in different contexts (e.g., create vs update).


## Example Controller

```java
package com.example.booklibrary.controller;

import com.example.booklibrary.model.Book;
import com.example.booklibrary.service.BookService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/books")
public class BookController 
{

    private final BookService service;

    public BookController(BookService service) 
    {
        this.service = service;
    }

    @PostMapping
    public Book addBook(@Valid @RequestBody Book book) 
    {
        return service.saveBook(book);
    }
}
```

- The `@Valid` annotation before `@RequestBody` tells Spring:

  > "Validate this object based on its annotations before executing the method."

- If validation fails, Spring automatically throws an exception and returns a **400 Bad Request**.
- The error will be "Bad request" if you don't handle it, but you can customize it to return more details about what went wrong.
- In order to show the error message you specified in entity or the DTO, you can create a `@ControllerAdvice` to handle `MethodArgumentNotValidException` and extract the error messages from the exception object.
- This way, you can provide a consistent error format for all validation errors across your API.
- If you forget to put @Valid in your controller, the server will give a 500 Internal Server Error instead of 400 Bad Request, because the validation will not be triggered and the service layer might throw an exception when it tries to save an invalid entity.

---

## ❌ Example Invalid Request

```json
{
  "title": "",
  "author": "  ",
  "year": 50,
  "genre": ""
}
```

### Response

```json
{
  "timestamp": "2025-11-03T14:00:00.000Z",
  "status": 400,
  "errors": [
    "Title is required",
    "Author is required",
    "Year must be realistic",
    "Genre is required"
  ]
}
```

---

## * Summary

| Feature                            | Purpose                            |
|------------------------------------|------------------------------------|
| `spring-boot-starter-validation`   | Enables input validation           |
| `@Valid`                           | Triggers validation in controllers |
| `jakarta.validation.constraints.*` | Provides validation annotations    |
| `@RestControllerAdvice`            | Handles validation errors globally |

---

## 💡 Best Practices

- Always validate incoming data (especially for `POST` and `PUT`).
  - Use `@Valid` annotation in controllers.///val'dated later
- Add custom messages to help API consumers understand the issue.
- Use `@ControllerAdvice` for consistent error handling.

---
