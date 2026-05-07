# 🔄 Mapper Layer

## DTO ↔ Entity Conversion · Clean Separation · No Logic in Controllers

The **mapper layer** converts between **entities** (database models) and **DTOs** (API request/response objects).

This keeps the system:

✔ Clean
✔ Maintainable
✔ Controller code simple
✔ DTOs isolated from database structure
✔ Entities isolated from API structure

---

# 🎬 MovieMapper

Responsible for converting:

* `MovieRequestDTO` → `Movie` (for create/update)
* `Movie` → `MovieResponseDTO` (for returning data to the client)

```java
package com.example.demo.mapper;

import com.example.demo.dto.MovieRequestDTO;
import com.example.demo.dto.MovieResponseDTO;
import com.example.demo.model.Movie;
import org.springframework.stereotype.Component;

@Component
public class MovieMapper
{
    // Convert DTO → Entity (used when creating/updating a movie)
    public Movie toEntity(MovieRequestDTO dto)
    {
        Movie movie = new Movie();
        movie.setTitle(dto.title);        // Copy title
        movie.setDirector(dto.director);  // Copy director
        movie.setYear(dto.year);          // Copy release year
        movie.setGenre(dto.genre);        // Copy genre
        return movie;
    }

    // Convert Entity → DTO (used when returning movie to client)
    public MovieResponseDTO toResponseDto(Movie movie)
    {
        MovieResponseDTO dto = new MovieResponseDTO();
        dto.id = movie.getId();           // Movie ID
        dto.title = movie.getTitle();     // Movie title
        dto.director = movie.getDirector();// Director
        dto.year = movie.getYear();       // Release year
        dto.genre = movie.getGenre();     // Genre
        return dto;
    }
}
```

✔ Keeps mapping consistent across the app
✔ Ensures DTOs never expose hidden database fields

---

# 👤 UserMapper (Optional but Recommended)

Used when returning user information to the frontend.
Prevents exposing password hashes or internal fields.

```java
package com.example.demo.mapper;

import com.example.demo.dto.RegisterRequestDTO;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper
{
    // Convert RegisterRequestDTO → User entity (used only during registration)
    public User fromRegisterDto(RegisterRequestDTO dto, String hashedPassword)
    {
        User user = new User();
        user.setUsername(dto.username);
        user.setEmail(dto.email);
        user.setHashedPassword(hashedPassword);

        // Default role = USER
        user.setRole(Role.USER);

        return user;
    }

    // Convert User → LoginResponseDTO if needed
}
```

---


