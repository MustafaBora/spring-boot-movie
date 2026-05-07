# * Validation in Controllers: `@Valid` vs `@Validated` + Global Exception Handling

Validation in Spring ensures that the data received from the client (JSON input, form data, etc.) meets the expected requirements
before it’s processed or stored in the database.

Spring Boot integrates with **Jakarta Bean Validation** (hibernate-validator under the hood).

---

## 🤔 Why validation in Controller?

Validation belongs to the **boundary of the system**.

Controller = entry point

👉 We reject bad data BEFORE it reaches business logic

---

## 1. `@Valid` — What Exception Does It Throw?

We already know `@Valid` triggers validation on a `@RequestBody` object. What is important here is what Spring throws when it fails:

```java
@PostMapping
public ResponseEntity<Contact> create(@Valid @RequestBody Contact contact)
{
    Contact saved = service.create(contact);
    return ResponseEntity.status(201).body(saved);
}
```

If validation fails → Spring throws:

```txt
MethodArgumentNotValidException
```

We need this name to handle it in the global exception handler.

---

## 2. `@Validated` — Validate **Path Variables & Request Parameters**

`@Validated` works similarly to `@Valid`, but **also enables validation at the method level**, including:

- `@RequestParam`
- `@PathVariable`

### Example

```java
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/contacts")
@Validated   // <--- enables validation on parameters
public class ContactController {

    @GetMapping("/{id}")
    public ResponseEntity<Contact> getById(@PathVariable @Min(1) Long id)
    {
        Contact contact = service.getById(id);
        return (contact != null)
                ? ResponseEntity.ok(contact)
                : ResponseEntity.notFound().build();
    }
}
```

If ID is less than 1, Spring throws:

```txt
ConstraintViolationException
```

We can also validate query parameters using a DTO with `@Valid`:

```java
@GetMapping("/search")
public ResponseEntity<List<ResponseContactDTO>> search(@Valid RequestContactDTO criteria)
{
    List<ResponseContactDTO> results = service.search(criteria);
    return results.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(results);
}
```

If the search criteria DTO has validation annotations, Spring checks them before the method runs.
If valid but no results are found, we return `404`. Otherwise `200` with the results.

---

## 3. When to Use Which?

| Situation | Use | Reason |
| --- | --- | --- |
| Validating `@RequestBody` JSON object | `@Valid` | Field-level model validation |
| Validating `@PathVariable` or `@RequestParam` | `@Validated` | Enables method-level validation |


---

## 4. * Global Exception Handling (Clean JSON Errors)

Spring controllers should not return validation exceptions directly.
We handle them **centrally** using `@RestControllerAdvice`.

Create:

```txt
src/main/java/.../exception/GlobalExceptionHandler.java
```

Two new annotations appear here for the first time:
- **`@ExceptionHandler(SomeException.class)`** — marks a method as the handler for a specific exception type. When Spring sees that exception thrown from any controller, it calls this method instead of returning a default error response.
- **`@ResponseStatus(HttpStatus.BAD_REQUEST)`** — sets the HTTP status code for this response. Without it, Spring would default to `200 OK` even for error responses.
- Without `@ResponseStatus` or `ResponseEntity`, Spring may not know which HTTP status code you want for this custom response. So we explicitly set it to `400 Bad Request`.

### Handle `@Valid` errors (RequestBody validation)

```java
@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ResponseStatus(HttpStatus.BAD_REQUEST) //400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationErrors(MethodArgumentNotValidException ex)
    {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errors.put(err.getField(), err.getDefaultMessage())
        );
        return errors;
    }
}
```

**Example error response:**

```json
{
  "email": "Email must be valid",
  "name": "Name cannot be blank"
}
```

---

### Handle `@Validated` param errors (path variables / request params)

```java
@ResponseStatus(HttpStatus.BAD_REQUEST) //400
@ExceptionHandler(ConstraintViolationException.class)
public Map<String, String> handleConstraintViolation(ConstraintViolationException ex)
{
    Map<String, String> errors = new HashMap<>();
    ex.getConstraintViolations().forEach(violation ->
            errors.put(
                violation.getPropertyPath().toString(),
                violation.getMessage()
            )
    );
    return errors;
}
```

---

## 5. Summary Table

| Validation Type     | Annotation   | Works On                         | Exception Type                    | Global Handler                | Status with Handler |
| ------------------- | ------------ | -------------------------------- | --------------------------------- | ----------------------------- | ------------------- |
| Request Body Object | `@Valid`     | `@RequestBody` DTO/entity        | `MethodArgumentNotValidException` | `handleValidationErrors()`    | 400 Bad Request     |
| Path / Query Param  | `@Validated` | `@PathVariable`, `@RequestParam` | `ConstraintViolationException`    | `handleConstraintViolation()` | 400 Bad Request     |

---

## * Best Practice

Always:

- ✔ Use `@Valid` on DTOs / request bodies
- ✔ Use `@Validated` on controllers if you want param validation
- ✔ Use **one global exception handler** to keep controllers clean

---
