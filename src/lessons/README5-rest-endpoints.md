# README – REST Endpoints with Spring Boot

This file introduces REST endpoints in a simple Spring Boot project.

The goal is to understand:
- what REST is and why we follow its conventions
- how to name endpoints well
- how HTTP methods map to controller methods
- how the controller, service, and model layers work together

---

## 1. What is REST?

REST stands for **Representational State Transfer**. It is not a protocol, not a library, and not a strict standard — it is a set of conventions for designing web APIs.

Roy Fielding described these conventions in his PhD dissertation in 2000. He was also one of the authors of the HTTP specification, so REST was shaped around what HTTP was already designed to do.

The core idea is straightforward: everything on the server is a **resource** (a user, a book, an order). We identify resources with URLs, and we use HTTP methods to say what we want to do with them.

```
GET    /books/1    ->  get book with id 1
DELETE /books/1    ->  delete book with id 1
```

The URL says **what**, the HTTP method says **how**.

### Before REST

Before REST became the common convention, two styles were widely used:

**RPC-style APIs** — the URL described the action, not the resource.
Each team invented their own patterns, so every API looked different.
```
/getUserById
/createOrder
/deleteAccount
```

**SOAP** — messages were wrapped in verbose XML with strict contracts.
It worked, but it was heavy and slow to work with. It is still used in some banking and enterprise systems today.

### Is REST required?

Nothing enforces REST. We can build a working API with `/getUsers` and `POST /deleteBook/3`. The app will still run.

In practice, REST is the default expectation for web APIs. Frameworks, documentation tools, and client libraries are all built with it in mind. When we follow REST conventions, our API is immediately familiar to any developer who has worked with web APIs before. When we ignore them, we introduce confusion that costs time.

---

## 2. What is an endpoint?

An endpoint is a URL in our application that handles an HTTP request.

Example:

``` text
GET /api/users
```

This means:
- the client sends a GET request to `/api/users`
- the backend returns user data

---

## 3. URL design — naming conventions

Since URLs identify resources, not actions, there are clear conventions for naming them well.

### Good practices

| # | Practice | Example |
|---|---|---|
| 1 | Use nouns, not verbs | `/users` not `/getUsers` |
| 2 | Use plural nouns for collections | `/orders` not `/order` |
| 3 | Use HTTP methods for the action | `DELETE /users/1` not `/users/1/delete` |
| 4 | Use path params to identify a resource | `/users/{id}` |
| 5 | Use query params for filtering | `/users?active=true` |
| 6 | Show relationships with hierarchy | `/users/{id}/orders` |
| 7 | Keep the same pattern across the API | `/users`, `/products`, `/orders` |
| 8 | Use kebab-case for multi-word names | `/order-items` |
| 9 | Version the API | `/api/v1/users` |
| 10 | Use sub-resources when an action is unavoidable | `/orders/{id}/cancel` |

### Common mistakes

| # | Problem | Example |
|---|---|---|
| 1 | Verb in the URL | `/getUsers` |
| 2 | RPC-style naming | `/createUser` |
| 3 | Singular noun for a collection | `/user` |
| 4 | Overly descriptive names | `/getAllUsersList` |
| 5 | Wrong HTTP method for the intent | `POST /users/getAll` |
| 6 | Path param used for filtering | `/users/active` instead of `/users?active=true` |
| 7 | Inconsistent naming across endpoints | `/users` but `/getOrders` |
| 8 | camelCase in URLs | `/getUserOrders` |
| 9 | No versioning | `/users` with no `v1` |
| 10 | Redundant action in URL | `/users/{id}/delete` |

### The rule

```
Noun + HTTP Method = REST

/users          OK
/getUsers       NOT OK
```

The HTTP method already carries the verb. The URL only needs to identify the resource.

---

## 4. Simple project structure

``` text
src/main/java/com/example/demo/
├── controller/
│   └── UserController.java
├── service/
│   └── UserService.java
├── model/
│   └── User.java
└── DemoApplication.java
```

At this stage:
- the controller handles HTTP
- the service contains business logic
- the model represents the data

The repository layer is added later when database integration starts.

---

## 5. Model class

``` java
package com.example.demo.model;

public class User {

    private Long id;
    private String name;
    private String email;

    public User() {
    }

    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
```

---

## 6. Service class

For now, we store data in memory using a `Map`. This is useful for learning but not for real persistence.

``` java
package com.example.demo.service;

import com.example.demo.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final Map<Long, User> users = new HashMap<>();

    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public User getUserById(Long id) {
        return users.get(id);
    }

    public User addUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    public void deleteUser(Long id) {
        users.remove(id);
    }
}
```

The service layer is where **separation of concerns** happens: the controller does not contain business logic, and the repository does not know about HTTP. Each layer has one job.

---

## 7. Controller class

``` java
package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    public User addUser(@RequestBody User user) {
        return userService.addUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
```

---

## 8. Annotation reference

### `@RestController`
Marks the class as a REST controller. It combines `@Controller` and `@ResponseBody`, which means every method returns JSON directly — no HTML page rendering.

### `@RequestMapping("/api/users")`
Sets the base path for all endpoints in this controller.

### `@GetMapping`, `@PostMapping`, `@DeleteMapping`, `@PutMapping`, `@PatchMapping`
Shortcuts for mapping HTTP methods to handler methods. Most developers use these instead of the longer `@RequestMapping(method = RequestMethod.GET)` form.

### `@PathVariable`
Reads a value from the URL path.

``` java
@GetMapping("/{id}")
public User getUserById(@PathVariable Long id) { ... }
```

In `GET /api/users/1`, the `1` is the path variable.

### `@RequestParam`
Reads a value from the query string.

``` java
@GetMapping
public List<User> getUsers(@RequestParam int page) { ... }
```

In `GET /api/users?page=2`, the `page=2` is the request param.

| | `@PathVariable` | `@RequestParam` |
|---|---|---|
| Where | `/users/{id}` | `/users?page=2` |
| Used for | identifying a specific resource | filtering, sorting, pagination |
| Required? | yes (part of the path) | optional by default |

### `@RequestBody`
Reads the JSON from the request body and converts it into a Java object. This conversion is called **deserialization**. When a method returns a Java object, Spring Boot converts it back to JSON — that is **serialization**. Both are handled automatically by a library called Jackson.

> We have seen serialization before in a different context — converting Java objects to binary or to a file. JSON serialization is the same concept with a different target format. For more on JSON itself, see README0-http.md.

---

## 9. Test examples

### Get all users

``` text
GET http://localhost:8080/api/users
```

### Add a user

``` text
POST http://localhost:8080/api/users
Content-Type: application/json

{
  "id": 1,
  "name": "Elena",
  "email": "elena@example.com"
}
```

### Get one user

``` text
GET http://localhost:8080/api/users/1
```

### Delete one user

``` text
DELETE http://localhost:8080/api/users/1
```

---

## 10. Current limitations

### Returning `null`
If a user does not exist, `getUserById` returns `null`. Spring Boot will still respond with `200 OK`, which is misleading.

A better approach:
- return `404 Not Found` when the resource does not exist
- use `ResponseEntity` to control the status code

### No input validation
Nothing prevents bad or missing data from being sent to the API. Validation can be added later with Bean Validation.

Both of these are topics for the next steps.

---

## 11. Mini project — Book Library API

Build a simple in-memory REST API for a book library. No database needed — the repository stores data in a `Map`.

### What we are building

An API where we can list books, find one by ID, add a new book, update a title, and delete a book.

---

### Step 0 — Create a new project

Go to [https://start.spring.io](https://start.spring.io) and configure:

| Field | Value |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | latest stable |
| Group | `com.example` |
| Artifact | `library` |
| Packaging | Jar |
| Java | 21 |

Dependencies to add: **Spring Web**

Click **Generate**, unzip the file, and open it in IntelliJ.

The project structure we will work in:

``` text
src/main/java/com/example/library/
├── controller/
│   └── BookController.java
├── service/
│   └── BookService.java
├── repository/
│   └── BookRepository.java
├── model/
│   └── Book.java
└── LibraryApplication.java
```

---

### Step 1 — Model

Create a `Book` class in the `model` package:

``` java
package com.example.library.model;

public class Book {

    private Long id;
    private String title;
    private String author;
    private int year;

    public Book() {}

    public Book(Long id, String title, String author, int year) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
    }

    // getters and setters for all fields
}
```

---

### Step 2 — Repository

The repository handles data storage. It uses a `Map` to simulate a database. Sample data is loaded in the constructor.

``` java
package com.example.library.repository;

import com.example.library.model.Book;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BookRepository {

    private final Map<Long, Book> data = new HashMap<>();

    public BookRepository() {
        data.put(1L, new Book(1L, "1984", "George Orwell", 1949));
        data.put(2L, new Book(2L, "Brave New World", "Aldous Huxley", 1932));
        data.put(3L, new Book(3L, "Fahrenheit 451", "Ray Bradbury", 1953));
        data.put(4L, new Book(4L, "Invisible Women", "Caroline Criado Perez", 2019));
        data.put(5L, new Book(5L, "Lord of The Rings", "J. R. R. Tolkien", 1954));
    }

    public List<Book> findAll() {
        return new ArrayList<>(data.values());
    }

    public Book findById(Long id) {
        return data.get(id);
    }

    public Book save(Book book) {
        data.put(book.getId(), book);
        return book;
    }

    public void deleteById(Long id) {
        data.remove(id);
    }
}
```

---

### Step 3 — Service

Implement these methods:

| Method | What it does |
|---|---|
| `List<Book> getAllBooks()` | returns all books |
| `Book getBookById(Long id)` | returns one book |
| `Book addBook(Book book)` | adds a book and returns it |
| `Book updateTitle(Long id, String newTitle)` | changes only the title |
| `void deleteBook(Long id)` | removes the book |

---

### Step 4 — Controller

Map the service methods to HTTP endpoints:

| Method | URL | What it does |
|---|---|---|
| `GET` | `/api/books` | list all books |
| `GET` | `/api/books/{id}` | get one book |
| `POST` | `/api/books` | add a new book |
| `PUT` | `/api/books/{id}` | update a book |
| `DELETE` | `/api/books/{id}` | delete a book |

---

### Step 5 — Test with Postman or Insomnia

The app already has 5 books loaded. Start with:

``` text
GET http://localhost:8080/api/books
```

Get one book:

``` text
GET http://localhost:8080/api/books/1
```

Add a new book:

``` text
POST http://localhost:8080/api/books
Content-Type: application/json

{
  "id": 6,
  "title": "The Handmaid's Tale",
  "author": "Margaret Atwood",
  "year": 1985
}
```

Update a title:

``` text
PUT http://localhost:8080/api/books/1?newTitle=Nineteen Eighty-Four
```

Delete a book:

``` text
DELETE http://localhost:8080/api/books/2
```

Verify the deletion with a final `GET /api/books`.

---

### Bonus tasks

- Add `GET /api/books/search?author=Orwell` to filter books by author
- If a book is not found, return a meaningful message instead of `null`
- Add a `GET /api/books/count` endpoint that returns the number of books

---

## 12. Exercise

Add these features to the User API from the earlier example:

1. `PUT /api/users/{id}` to update a user
2. `GET /api/users/search?name=Jullie` to search by name
3. Return a message when a user is not found

Bonus: discuss which layer should contain the search logic, and why.

---

## 13. Quick summary

### Layers

| Layer | Responsibility |
|---|---|
| **Controller** | Handles HTTP requests and responses |
| **Service** | Contains business logic |
| **Repository** | Handles data storage (database or in-memory) |
| **Model** | Represents the data structure |

### Key annotations

| Annotation | What it does |
|---|---|
| `@RestController` | Marks a class as a REST controller; returns JSON by default |
| `@RequestMapping` | Sets the base URL path for the controller |
| `@GetMapping` / `@PostMapping` etc. | Maps an HTTP method to a handler method |
| `@PathVariable` | Reads a value from the URL path (`/users/{id}`) |
| `@RequestParam` | Reads a value from the query string (`/users?page=2`) |
| `@RequestBody` | Deserializes the JSON request body into a Java object |

### REST naming in one line

```
Noun + HTTP Method = REST

/users       -> GET /users = get all users     OK
/getUsers    -> verb in the URL                NOT OK
```
