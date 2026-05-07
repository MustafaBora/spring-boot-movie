# 🎮 **Controller Layer**

## Authentication · Token Validation · User Ownership · Admin Override

The Controller Layer exposes the public REST API endpoints.
Controllers do **not** contain business logic—
they **validate incoming requests**,
**ensure authentication is provided**,
and **pass the authenticated User** into the service layer.

## ✔ Controllers in this Project

| Controller          | Responsibilities                         |
|---------------------|------------------------------------------|
| **AuthController**  | Registration, Login, issuing JWT tokens  |
| **MovieController** | Public movie reading + secure movie CRUD |

---

# 🔐 **AuthController — Registration & Login**

Handles user registration and login.

📌 **Public Routes (No token required):**

* `POST /auth/register`
* `POST /auth/login`

Registration validates:

* Email uniqueness
* Password length
* Username non-empty

Login returns:

* JWT token
* User role
* User ID

---

## **AuthController.java**

```java
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // -------------------------------------------------------
    // POST /auth/register  → PUBLIC
    // -------------------------------------------------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO dto) {

        authService.register(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

    // -------------------------------------------------------
    // POST /auth/login → PUBLIC
    // Returns a JWT token and user info
    // -------------------------------------------------------
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto
    ) {
        LoginResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }
}
```

---

# 🎬 **MovieController — PUBLIC & PROTECTED Routes**

This controller follows your project rules:

### ✔ Public Routes

* `GET /movies`
* `GET /movies/{id}`

### ✔ USER Rules

* Can create movies under his own account
* Can update only *his* movies
* Can delete only *his* movies

### ✔ ADMIN Rules

* Can create ANY movie
* Can update ANY movie
* Can delete ANY movie

### ✔ Ownership Validation

Ownership enforcement happens in:

```
MovieService.updateMovie(...)
MovieService.deleteMovie(...)
```

Not in the controller — this keeps the controller clean.

---

## **MovieController.java**

```java
@RestController
@RequestMapping("/movies")
public class MovieController 
{
    private final MovieService movieService;

    public MovieController(MovieService movieService) 
    {
        this.movieService = movieService;
    }

    // -------------------------------------------------------
    // GET /movies → PUBLIC
    // Anyone can view all movies
    // -------------------------------------------------------
    @GetMapping
    public List<MovieResponseDTO> getAllMovies() 
    {
        return movieService.getAllMovies();
    }

    // -------------------------------------------------------
    // GET /movies/{id} → PUBLIC
    // Anyone can view one movie
    // -------------------------------------------------------
    @GetMapping("/{id}")
    public MovieResponseDTO getMovieById(@PathVariable Long id) 
    {
        return movieService.getMovieById(id);
    }

    // -------------------------------------------------------
    // POST /movies → USER & ADMIN
    // USER: creates own movie
    // ADMIN: creates any movie
    // Note: user is never null here — SecurityConfig already
    // rejects unauthenticated requests before reaching this method.
    // -------------------------------------------------------
    @PostMapping
    public ResponseEntity<?> createMovie(
            @Valid @RequestBody MovieRequestDTO dto,
            @AuthenticationPrincipal User user
    ) 
    {
        MovieResponseDTO created = movieService.createMovie(dto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // -------------------------------------------------------
    // PUT /movies/{id} → USER & ADMIN
    // USER: update only own movie
    // ADMIN: update any movie
    // -------------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieRequestDTO dto,
            @AuthenticationPrincipal User user
    ) 
    {
        MovieResponseDTO updated = movieService.updateMovie(id, dto, user);
        return ResponseEntity.ok(updated);
    }

    // -------------------------------------------------------
    // DELETE /movies/{id} → USER & ADMIN
    // USER: delete only own movie
    // ADMIN: delete any movie
    // -------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMovie(
            @PathVariable Long id,
            @AuthenticationPrincipal User user
    ) 
    {
        movieService.deleteMovie(id, user);
        return ResponseEntity.ok("Movie deleted successfully");
    }
}
```

---

