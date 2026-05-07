# Custom Exceptions and the Error Flow

README32 covered how to handle **validation exceptions** (`MethodArgumentNotValidException`, `ConstraintViolationException`) in the global handler. Those exceptions are thrown automatically by Spring when `@Valid` or `@Validated` fails.

This file covers the other half: exceptions that **you throw yourself** from the service layer — such as "entity not found" or "email already registered".

---

## The Error Flow

```
Exception thrown in Service (or anywhere)
  → propagates up the call stack
  → caught by GlobalExceptionHandler  (@RestControllerAdvice)
  → returns ResponseEntity with error body and correct HTTP status
  → Client receives a clean JSON error
```

The global handler is the **single exit point** for all errors. Controllers do not catch exceptions — they let them propagate.

**Propogate means** if an exception is thrown in the service, it goes up to the controller, then to the global handler. The controller does not catch it.

---

## Why Not Just Use `RuntimeException` Directly?

You could throw `new RuntimeException("Movie not found")` from a service. The handler catches it.

The problem: `RuntimeException` is too broad. If the handler catches `RuntimeException`, it will also accidentally catch unexpected bugs (null pointers, database errors) and return them as `404 Not Found` — which is wrong.

The solution: create a **specific exception class** for each error scenario.

---

## Step 1 — Create a Custom Exception

```txt
src/main/java/.../exception/MovieNotFoundException.java
```

```java
public class MovieNotFoundException extends RuntimeException
{
    public MovieNotFoundException(Long id)
    {
        super("Movie not found with id: " + id);
    }
}
```

Extending `RuntimeException` means Spring does not require you to declare it with `throws` — it is an **unchecked exception**.

---

## Step 2 — Throw It from the Service

```java
@Service
public class MovieService
{
    private final MovieRepository repo;

    public MovieDTO getById(Long id)
    {
        Movie movie = repo.findById(id)
                .orElseThrow(() -> new MovieNotFoundException(id));
        return mapper.toDTO(movie);
    }
}
```

`orElseThrow()` is a method on `Optional<T>` — if the value is present it returns it, if empty it throws the exception you provide.

---

## Step 3 — Handle It in the Global Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler
{
    // ... validation handlers from README32 ...

    @ResponseStatus(HttpStatus.NOT_FOUND)   // 404
    @ExceptionHandler(MovieNotFoundException.class)
    public Map<String, String> handleMovieNotFound(MovieNotFoundException ex)
    {
        return Map.of("error", ex.getMessage());
    }
}
```

Now a `GET /movies/999` for a non-existent movie returns:

```json
HTTP 404 Not Found
{
  "error": "Movie not found with id: 999"
}
```

## Alternative: Everything in One Handler

All handlers live inside the **same** `GlobalExceptionHandler` class. The validation handlers from README32 and the custom exception handlers from this file combine into one class:

> **Note on the last method below:** `RuntimeException.class` is used here as a catch-all for any unhandled runtime exception. Prefer a specific exception class (e.g. `MovieNotFoundException`) over this — catching `RuntimeException` broadly can accidentally mask real bugs and return them with the wrong status code.

```java
@RestControllerAdvice
public class GlobalExceptionHandler
{

    // Handles @Valid failures on @RequestBody fields
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationErrors(MethodArgumentNotValidException ex)
    {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage()));
        return errors;
    }

    // Handles @Validated failures on @PathVariable / @RequestParam
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Map<String, String> handleConstraintViolation(ConstraintViolationException ex)
    {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(v ->
                errors.put(v.getPropertyPath().toString(), v.getMessage()));
        return errors;
    }

    // --- Custom exception handlers ---

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(MovieNotFoundException.class)
    public Map<String, String> handleMovieNotFound(MovieNotFoundException ex)
    {
        return Map.of("error", ex.getMessage());
    }

    // Catch-all for any other RuntimeException not handled above.
    // prefer specific exception classes over this
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleRuntime(RuntimeException ex)
    {
        return Map.of("error", ex.getMessage());
    }
}
```

---

## Common Custom Exceptions and Their Status Codes

| Scenario                  | Exception class               | HTTP Status          |
|---------------------------|-------------------------------|----------------------|
| Resource does not exist   | `MovieNotFoundException`      | 404 Not Found        |
| Email already registered  | `EmailAlreadyExistsException` | 409 Conflict         |
| Caller is not authorised  | `UnauthorisedException`       | 403 Forbidden        |
| Invalid business rule     | `InvalidOperationException`   | 400 Bad Request      |

Create one class per scenario. Each gets its own `@ExceptionHandler` method in the global handler.

---

## Summary

| Concept | Meaning |
|---|---|
| Custom exception | A class extending `RuntimeException` for a specific error scenario |
| `orElseThrow()` | Throws if the `Optional` is empty |
| `@ExceptionHandler` | Catches a specific exception type in the global handler |
| `@ResponseStatus` | Sets the HTTP status code for the response |

---

