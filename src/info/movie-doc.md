# 🎬 Secure Movie Library API

## Spring Boot 4 · JWT Authentication · Role-Based Access · User-Owned Resources

A fully secure REST API built with:

* **Spring Boot 4**
* **Spring Security + JWT**
* **User Roles (USER & ADMIN)**
* **Entity ↔ DTO Mapping**
* **Global Exception Handling**
* **User-Owned Resource Authorization**

---

# ⭐ Project Requirements

### ✔ Public Routes

* `POST /auth/register`
* `POST /auth/login`
* `GET /movies`
* `GET /movies/{id}`

### ✔ USER Permissions

A regular **USER** can:

| Action          | Allowed?                  |
|-----------------|---------------------------|
| Create movie    | ✔ YES (only his own)      |
| View his movies | ✔ YES                     |
| Update movie    | ✔ ONLY IF he is the owner |
| Delete movie    | ✔ ONLY IF he is the owner |

### ✔ ADMIN Permissions

ADMIN can:

| Action                         | Allowed? |
|--------------------------------|----------|
| Create/update/delete any movie | ✔ YES    |
| View all movies                | ✔ YES    |

---

# 🧱 Project Architecture

```
src/
 ├─ controller/         ← REST endpoints
 ├─ service/            ← Business logic
 ├─ repository/         ← Database queries
 ├─ mapper/             ← Entity ↔ DTO conversion
 ├─ dto/                ← Request/response objects
 ├─ model/              ← JPA entities + relationships
 ├─ security/           ← JWT, filters, auth config
 ├─ exception/          ← Centralized error handling
```

---

# 🔐 Authentication Module

## JWT Includes:

* `userId`
* `email`
* `role`

### JwtUtils

* Signs JWTs using **Base64 HMAC-SHA256**
* Validates tokens
* Extracts subject and claims

---

# 👤 User Model

```
User 1 --- * Movie
```

Each user owns multiple movies.

Admin also owns movies but can modify ALL movies.

---

# 🎬 Movie Model

* Belongs to exactly **one user**
* Uses JSON annotations to prevent recursion

---

# ⭐ DTO Layer

### Request DTOs (validated)

* `RegisterRequestDTO`
* `LoginRequestDTO`
* `MovieRequestDTO`

### Response DTOs

* `LoginResponseDTO`
* `MovieResponseDTO`

---

# 🔄 Mapper Layer

### MovieMapper

Converts between:

* DTO → Entity (create/update)
* Entity → DTO (response)

### UserMapper

Used during registration.

---

# 🚨 Exception Layer

Centralized handlers for:

* Validation errors
* Illegal arguments
* Movie not found
* Unauthorized / forbidden operations

Custom exceptions:

* `MovieNotFoundException`
* `ForbiddenActionException`

---

# 🧠 Service Layer (Business Logic)

## AuthService

Handles:

* Register
* Login
* Password hashing
* JWT creation

## MovieService

Handles:

* ADMIN can manage ANY movie
* USER can only manage **his own** movies

### Ownership Rule

```java
if (user.getRole() == Role.USER && movie.getOwner().getId() != user.getId()) 
{
    throw new ForbiddenActionException("You do not own this movie");
}
```

---

# 🎮 Controller Layer (REST API)

## AuthController

| Method | Route          | Description |
|--------|----------------|-------------|
| POST   | /auth/register | Public      |
| POST   | /auth/login    | Public      |

Uses `@Valid` DTO validation.

---

## MovieController

Uses:

* `@AuthenticationPrincipal User user`
* Role + ownership checks
* 401 for invalid/missing token
* 403 for unauthorized (not admin / not owner)

### USER Rules

✔ can CRUD only his movies

### ADMIN Rules

✔ can CRUD all movies

---

# 🔥 Security Module

### JWTAuthenticationFilter

* Reads Authorization header
* Validates token
* Loads user
* Sets authentication context

### SecurityConfig

* Public auth endpoints
* Public movie GET
* Protected movie CRUD
* BCrypt password encoder

---

# 📦 Repository Layer

### UserRepository

* `findByEmail`
* `existsByEmail`

### MovieRepository

* `findByOwnerId`
* Custom query for admin access (optional)

---

# 🧪 Example API Flow

## 1️⃣ Register

POST `/auth/register`

```json
{
  "username": "sam",
  "email": "sam@mail.com",
  "password": "123456"
}
```

## 2️⃣ Login

POST `/auth/login`

Returns:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIs..."
}
```

## 3️⃣ Create Movie (USER)

POST `/movies`
Header:

```
Authorization: Bearer <TOKEN>
```

Body:

```json
{
  "title": "Inception",
  "director": "Nolan",
  "year": 2010,
  "genre": "Sci-Fi"
}
```

USER can ONLY modify movies he created.

---

# 🛡 Ownership Check (Important)

User tries to update someone else’s movie:

```
PUT /movies/5
```

Response:

```json
{
  "error": "You do not own this movie"
}
```

Admin trying same action → allowed.

---

# 📘 Database Schema (JPA Auto)

### User Table

| Field          | Type   |
|----------------|--------|
| id             | PK     |
| username       | string |
| email          | string |
| hashedPassword | string |
| role           | enum   |

### Movie Table

| Field    | Type        |
|----------|-------------|
| id       | PK          |
| title    | string      |
| director | string      |
| year     | int         |
| genre    | string      |
| user_id  | FK to users |

---

# ☑ Final Summary

### USER

✔ Can only view/create/update/delete **his own** movies
✔ Cannot touch other users’ movies
✔ Cannot access admin features

### ADMIN

✔ Full CRUD over ALL movies
✔ Full read access over all movies

---

