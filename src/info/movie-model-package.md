# 🏗️ Model Layer — Movie API

## JPA Entities · Validation Constraints · Relationships · Constructors

The **model layer** contains your database entities.
These represent the actual tables stored in the database.

In this project you have two main models:

* **User**
* **Movie**

With the relationship:

```
User 1 —— * Movie
```

(One user can have many movies)

Typically:

* **Users** create movies
* **Admins** can modify or delete ANY movie
* The relationship does NOT depend on role

---

# 👤 User Entity

Represents a registered user in the system.

```java
package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;     // Primary key

    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 40)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(unique = true)
    private String email;

    @NotBlank
    private String hashedPassword;   // Stored encrypted password

    @Enumerated(EnumType.STRING)
    private Role role;               // USER or ADMIN

    // One user can have many movies
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Movie> movies;

    // --- Constructors ---
    public User() {}

    public User(String username, String email, String hashedPassword, Role role)
    {
        this.username = username;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.role = role;
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public List<Movie> getMovies() { return movies; }
    public void setMovies(List<Movie> movies) { this.movies = movies; }
}
```

---

# 🎬 Movie Entity

Represents a movie in the database.

```java
package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "movies")
public class Movie
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;      // Primary key

    @NotBlank(message = "Title is required")
    @Size(min = 2, max = 100)
    private String title;

    @NotBlank(message = "Director is required")
    @Size(min = 2, max = 50)
    private String director;

    @NotNull(message = "Year is required")
    @Min(1888)
    @Max(2100)
    private Integer year;

    @NotBlank(message = "Genre is required")
    @Size(min = 3, max = 30)
    private String genre;

    // Many movies belong to one owner
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "user_id")  // Foreign key
    private User owner;

    // --- Constructors ---
    public Movie() {}

    public Movie(String title, String director, Integer year, String genre, User owner)
    {
        this.title = title;
        this.director = director;
        this.year = year;
        this.genre = genre;
        this.owner = owner;
    }

    // --- Getters & Setters ---
    public Long getId() { return id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
}
```

---

# ℹ️ Should Admin Have Movies?

Admins **can** own movies, but they don’t need to.

Typical rules:

* **Users** create movies → movie.owner = the user
* **Admins** can edit or delete ANY movie (but ownership stays with the user who created it)




