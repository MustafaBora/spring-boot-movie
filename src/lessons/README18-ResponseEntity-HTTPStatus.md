# 📘 Understanding `ResponseEntity` and `HttpStatus` in Spring Boot

## 🧩 What Is `ResponseEntity`?

`ResponseEntity` is a **Spring class** that represents the **entire HTTP response** — including:

* **Status code** (e.g., `200 OK`, `404 Not Found`)
* **Headers** (e.g., `Content-Type`, `Authorization`)
* **Body** (the actual response data)

It gives you **full control** over what is returned to the client (such as a frontend app or API consumer).

### * Syntax Example

```java
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tasks")
public class TaskController 
{

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getAllTasks() {
        List<Task> tasks = service.getAllTasks();

        if (tasks.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok(tasks);
    }
}
```

### 💡 Explanation

* `ResponseEntity.ok(tasks)` → returns **HTTP 200 OK** and includes `tasks` in the body.
* `ResponseEntity.status(HttpStatus.NO_CONTENT)` → returns **HTTP 204 No Content**

---

## Why Use `ResponseEntity`?

| Purpose                   | Benefit                                                                   |
|---------------------------|---------------------------------------------------------------------------|
| Control over status codes | You decide whether to send `200 OK`, `201 Created`, `404 Not Found`, etc. |
| Include custom headers    | Add authentication tokens, CORS headers, or caching instructions.         |
| Flexible response types   | You can send plain text, JSON, or even a custom object.                   |
| Better RESTful design     | Aligns with HTTP standards for clear API responses.                       |

---

## 🏷️ Common `HttpStatus` Codes in Spring Boot

`HttpStatus` is an **enumeration (enum)** in Spring (`org.springframework.http.HttpStatus`) that defines all standard HTTP status codes.

Below are the most commonly used ones, grouped by category:

---

### * **1xx — Informational**

| Code | Constant   | Meaning                            |
|------|------------|------------------------------------|
| 100  | `CONTINUE` | Request received, continue process |

---

### 🟩 **2xx — Success**

| Code | Constant     | Meaning                                            |
|------|--------------|----------------------------------------------------|
| 200  | `OK`         | Successful request                                 |
| 201  | `CREATED`    | Resource created successfully                      |
| 202  | `ACCEPTED`   | Request accepted for processing, but not completed |
| 204  | `NO_CONTENT` | Request successful, but no content returned        |

🧠 **Example:**

```java
return ResponseEntity.status(HttpStatus.CREATED).body(newTask);
```

---

### 🟨 **3xx — Redirection**

| Code | Constant            | Meaning                                  |
|------|---------------------|------------------------------------------|
| 301  | `MOVED_PERMANENTLY` | Resource moved to a new URL              |
| 302  | `FOUND`             | Temporary redirect                       |
| 304  | `NOT_MODIFIED`      | Resource not modified since last request |

---

### 🟥 **4xx — Client Errors**

| Code | Constant               | Meaning                                    |
|------|------------------------|--------------------------------------------|
| 400  | `BAD_REQUEST`          | Invalid request syntax or parameters       |
| 401  | `UNAUTHORIZED`         | Authentication required or failed          |
| 403  | `FORBIDDEN`            | Server understood but refuses to authorize |
| 404  | `NOT_FOUND`            | Resource not found                         |
| 409  | `CONFLICT`             | Conflict with current state of resource    |
| 422  | `UNPROCESSABLE_ENTITY` | Validation failed                          |

🧠 **Example:**

```java
return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task not found");
```

---

### ⛔ **5xx — Server Errors**

| Code | Constant                | Meaning                               |
|------|-------------------------|---------------------------------------|
| 500  | `INTERNAL_SERVER_ERROR` | Generic server error                  |
| 502  | `BAD_GATEWAY`           | Invalid response from upstream server |
| 503  | `SERVICE_UNAVAILABLE`   | Server temporarily unavailable        |
| 504  | `GATEWAY_TIMEOUT`       | Server did not respond in time        |

🧠 **Example:**

```java
return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                     .body("Unexpected error occurred");
```

---

## Common ResponseEntity Methods

| Method                 | Description                                     |
|------------------------|-------------------------------------------------|
| `ok(body)`             | Returns `200 OK` with a body                    |
| `status(HttpStatus)`   | Set custom status                               |
| `badRequest()`         | Shortcut for `400 Bad Request`                  |
| `notFound()`           | Shortcut for `404 Not Found`                    |
| `noContent()`          | Shortcut for `204 No Content`                   |
| `created(URI)`         | Shortcut for `201 Created` with location header |
| `body(Object)`         | Sets the response body                          |
| `headers(HttpHeaders)` | Adds custom headers                             |

🧩 **Example with headers:**

```java
HttpHeaders headers = new HttpHeaders();
headers.add("Custom-Header", "TaskAPI");

return ResponseEntity
        .status(HttpStatus.OK)
        .headers(headers)
        .body("Header added successfully");
```

---

## ⚙️ Example: CRUD Responses in a REST API

| Action         | Status Code                  | Response                      |
|----------------|------------------------------|-------------------------------|
| Create Task    | `201 CREATED`                | Returns created task          |
| Get All Tasks  | `200 OK` or `204 NO_CONTENT` | Returns list or empty message |
| Get Task by ID | `200 OK` or `404 NOT_FOUND`  | Returns task or error         |
| Update Task    | `200 OK`                     | Returns updated task          |
| Delete Task    | `204 NO_CONTENT`             | No body needed                |

---

## 🧾 Example: Error Handling

```java
@GetMapping("/{id}")
public ResponseEntity<?> getTaskById(@PathVariable Long id) {
    Optional<Task> task = service.getTaskById(id);

    if (task.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body("Task not found with ID: " + id);
    }

    return ResponseEntity.ok(task.get());
}
```

---

## 📚 Summary

| Feature              | Description                                             |
|----------------------|---------------------------------------------------------|
| **`ResponseEntity`** | Represents a full HTTP response (status, headers, body) |
| **`HttpStatus`**     | Enum containing all standard HTTP status codes          |
| **Usage**            | Used in REST controllers to send proper HTTP responses  |
| **Benefit**          | Makes APIs more readable, maintainable, and RESTful     |

---

## 🔗 Official Documentation

* [Spring Framework — ResponseEntity](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/ResponseEntity.html)
* [Spring Framework — HttpStatus Enum](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/http/HttpStatus.html)

---
