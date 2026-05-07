# ⚠️ Exception Layer 

### Centralized Error Handling · Validation Errors · Custom Exceptions

The **Exception Layer** is responsible for handling errors in a clean, consistent, and centralized way.
Instead of scattering `try/catch` blocks across controllers, all exceptions are handled inside one class:

```
GlobalExceptionHandler
```

This gives you:

✔ Clean controllers
✔ Standardized error responses
✔ Nice readable JSON errors
✔ Automatic validation messages
✔ Custom exceptions for missing resources

---

# 📌 GlobalExceptionHandler (with comments)

```java
package com.example.demo.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestControllerAdvice   // Makes this class listen for exceptions globally
public class GlobalExceptionHandler
{
    // Handles errors like: throw new IllegalArgumentException("Message");
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex)
    {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    // Handles all unexpected runtime errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handle(RuntimeException ex) 
    {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    // Handles validation errors from @Valid annotations in DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) 
    {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
            .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

        return ResponseEntity.badRequest().body(errors);
    }

    // 🔥 Custom: Movie Not Found
    @ExceptionHandler(MovieNotFoundException.class)
    public ResponseEntity<?> handleMovieNotFound(MovieNotFoundException ex)
    {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    // 🔥 Custom: User Not Found
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFoundException ex)
    {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    // 🔥 Custom: Email already exists on registration
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<?> handleEmailExists(EmailAlreadyExistsException ex)
    {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)  // 409 Conflict
                .body(Map.of("error", ex.getMessage()));
    }
}
```

---

# 🧨 Custom Exceptions (clean + simple)

## 1️⃣ MovieNotFoundException

```java
package com.example.demo.exception;

// Thrown when a movie is missing from the database
public class MovieNotFoundException extends RuntimeException 
{
    public MovieNotFoundException(Long id) 
    {
        super("Movie with ID " + id + " not found.");
    }
}
```

---

## 2️⃣ UserNotFoundException

```java
package com.example.demo.exception;

// Thrown when user lookup fails
public class UserNotFoundException extends RuntimeException 
{
    public UserNotFoundException(Long id) 
    {
        super("User with ID " + id + " not found.");
    }

    public UserNotFoundException(String email) 
    {
        super("User with email " + email + " not found.");
    }
}
```

---

## 3️⃣ EmailAlreadyExistsException

```java
package com.example.demo.exception;

// Thrown during registration when email is already in use
public class EmailAlreadyExistsException extends RuntimeException 
{
    public EmailAlreadyExistsException(String email) 
    {
        super("Email already exists: " + email);
    }
}
```

---

# 🧠 How Exceptions Work Together

### In the **service layer**, you write:

```java
Movie movie = movieRepository.findById(id)
    .orElseThrow(() -> new MovieNotFoundException(id));
```

Or:

```java
if (userRepository.existsByEmail(dto.email)) 
{
    throw new EmailAlreadyExistsException(dto.email);
}
```

Then, the **GlobalExceptionHandler** catches these automatically and returns clean JSON:

```json
{
  "error": "Movie with ID 10 not found."
}
```

---

# 🎯 Summary

Your exception layer now supports:

| Exception                     | Status Code | Purpose            |
|-------------------------------|-------------|--------------------|
| `IllegalArgumentException`    | 400         | Generic errors     |
| `Validation Errors`           | 400         | DTO validation     |
| `MovieNotFoundException`      | 404         | Missing movie      |
| `UserNotFoundException`       | 404         | Missing user       |
| `EmailAlreadyExistsException` | 409         | Register duplicate |
| `RuntimeException`            | 400         | Catch-all          |

This makes your API **clean, safe, and production-ready**.

---

