# 📦 DTO Layer

## Movie API · Request/Response DTOs · Secure & Clean Input Validation

This layer defines the Data Transfer Objects (DTOs) used to **receive** and **return** data in your API.
All *request DTOs* now include strong **validation constraints** using:

```java
import jakarta.validation.constraints.*;
```

This ensures data coming into the API is valid, secure, and clean.

---

# 🎬 Movie DTOs

---

## 1️⃣ MovieRequestDTO

Used when **creating or updating a movie**.

```java
package com.example.demo.dto;

import jakarta.validation.constraints.*;

// Sent from client → API to create or update a movie
public class MovieRequestDTO 
{
    @NotBlank(message = "Title is required")      // Cannot be null or empty
    @Size(min = 2, max = 100, message = "Title must be 2–100 characters")
    public String title;

    @NotBlank(message = "Director name is required")
    @Size(min = 2, max = 50, message = "Director name must be 2–50 characters")
    public String director;

    @NotNull(message = "Year is required")
    @Min(value = 1888, message = "Year must be after 1888")   // First movie ever
    @Max(value = 2100, message = "Year must be before 2100")
    public Integer year;

    @NotBlank(message = "Genre is required")
    @Size(min = 3, max = 30, message = "Genre must be 3–30 characters")
    public String genre;
    
    // Getters and Setters (if needed) can be added here
}
```

✔ Prevents invalid or empty movie submissions
✔ Guarantees reasonable limits (title length, year range, etc.)

---

## 2️⃣ MovieResponseDTO

Used when **returning a movie** to the client.
(No constraints needed here — it is output-only.)

```java
package com.example.demo.dto;

public class MovieResponseDTO 
{
    public Long id;
    public String title;
    public String director;
    public Integer year;
    public String genre;
    
    // Getters and Setters (if needed) can be added here
}
```

---

# 🔐 Authentication DTOs

---

## 3️⃣ LoginRequestDTO

Used when **logging in**.

```java
package com.example.demo.dto;

import jakarta.validation.constraints.*;

// Sent from client → API during login
public class LoginRequestDTO 
{
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    public String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 50, message = "Password must be between 6–50 characters")
    public String password;
    
    // Getters and Setters (if needed) can be added here
}
```

✔ Ensures email format
✔ Ensures password is not empty or too short

---

## 4️⃣ LoginResponseDTO

Sent after successful login.

```java
package com.example.demo.dto;

public class LoginResponseDTO {
    public String token;  // JWT token for authenticated requests

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
}
```

---

## 5️⃣ RegisterRequestDTO

Used when creating a new user account.

```java
package com.example.demo.dto;

import jakarta.validation.constraints.*;

// Sent from client → API during user registration
public class RegisterRequestDTO 
{
    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 40, message = "Username must be 2–40 characters")
    public String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    public String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6–100 characters")
    public String password;
    
    // Getters and Setters (if needed) can be added here
}
```
---




