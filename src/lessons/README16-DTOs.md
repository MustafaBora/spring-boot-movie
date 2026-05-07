# 📦 Understanding DTOs (Data Transfer Objects)

## What is a DTO?

A **DTO (Data Transfer Object)** is a simple Java class used to **transfer data** between layers of our application, especially between:

```txt
Client → Controller → Service → Database
```

DTOs allow us to **control exactly what data is received and returned** in the API.

---

## Why Not Use Entities Directly?

Your **Entity** classes are tied to the **database**. They often include:

* JPA annotations (`@Entity`, `@OneToMany`, etc.)
* Internal identifiers
* Fields you don’t want exposed
* Relationships & DB-specific details

If we expose them directly in our API:

| Problem                                 | Example                                         |
|-----------------------------------------|-------------------------------------------------|
| Sensitive data can leak                 | Password, internal ID                           |
| Frontend becomes dependent on DB schema | Harder to change DB later                       |
| Cannot control API structure            | Fields sent to/from client cannot be restricted |
| Harder to validate input                | User could update fields they shouldn’t         |

✅ Using **DTOs prevents these issues.**

---

## * Two Types of DTOs

| Type             | Purpose                                            |
|------------------|----------------------------------------------------|
| **Request DTO**  | Used when the **client sends data** (POST/PUT)     |
| **Response DTO** | Used when the **API returns data** (GET endpoints) |

---

## 📝 Example: Contact API

### Entity (Database Model)

This is what the full entity might look like in the database. Notice how many fields exist that we would **never** want to expose in an API response:

```java
@Entity
public class Contact 
{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String email;

    // Security — never expose this
    private String passwordHash;

    // GDPR — sensitive personal data
    private String nationalIdNumber;       // BSN / SSN etc.
    private LocalDate dateOfBirth;
    private String medicalNotes;

    // Internal DB / audit fields — not relevant to the client
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByInternalUser;
    private boolean isDeleted;             // soft-delete flag
    private int failedLoginAttempts;

    // Relations — can trigger lazy-loading issues or expose too much
    @OneToMany(mappedBy = "contact")
    private List<Order> orders;

    // getters & setters
}
```

If we returned this entity directly from our controller, the client would receive **all of this** — passwords, national ID numbers, medical notes, internal flags. That is a security and GDPR violation.

### Request DTO (Client → Server)

The client only needs to send the fields required to **create** a contact. Nothing else:

```java
public class ContactRequestDTO 
{
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone is required")
    private String phone;

    @Email @NotBlank(message = "Email is required")
    private String email;

    // getters & setters
}
```

> The client cannot set `passwordHash`, `nationalIdNumber`, `createdAt`, or any internal field — they simply don't exist in this DTO.

### Response DTO (Server → Client)

We only return what the client actually needs to display:

```java
public class ContactResponseDTO 
{
    private Long id;
    private String name;
    private String phone;
    private String email;

    // getters & setters
}
```

> `passwordHash`, `medicalNotes`, `isDeleted`, `failedLoginAttempts` — none of these are here. They stay safely inside the service and database layer.

---

## 🔄 Converting Entity ↔ DTO (Inside Service Layer)

```java
@Service
public class ContactService 
{

    public ContactResponseDTO toDTO(Contact c) 
    {
        ContactResponseDTO dto = new ContactResponseDTO();
        dto.setId(c.getId());
        dto.setName(c.getName());
        dto.setPhone(c.getPhone());
        dto.setEmail(c.getEmail());
        return dto;
    }

    public Contact toEntity(ContactRequestDTO dto) 
    {
        Contact c = new Contact();
        c.setName(dto.getName());
        c.setPhone(dto.getPhone());
        c.setEmail(dto.getEmail());
        return c;
    }
}
```

---

## 🌍 Controller Example Using DTOs

```java
@PostMapping("/contacts")
public ResponseEntity<ContactResponseDTO> create(@Valid @RequestBody ContactRequestDTO dto) 
{
    Contact saved = service.save(service.toEntity(dto));
    return ResponseEntity.status(201).body(service.toDTO(saved));
}
```

---

## 🍽 Example: Recipe API DTOs

### Request DTO

```java
public class RecipeRequestDTO 
{
    @NotBlank private String title;
    @NotBlank private String ingredients;
    @NotBlank private String category;
}
```

### Response DTO

```java
public class RecipeResponseDTO
{
    private Long id;
    private String title;
    private String ingredients;
    private String category;
}
```

### Convert inside Service

```java
private RecipeResponseDTO toDTO(Recipe r) 
{
    RecipeResponseDTO dto = new RecipeResponseDTO();
    dto.setId(r.getId());
    dto.setTitle(r.getTitle());
    dto.setIngredients(r.getIngredients());
    dto.setCategory(r.getCategory());
    return dto;
}
```

---

## Key Benefits of DTOs

| Benefit                    | Explanation                                        |
|----------------------------|----------------------------------------------------|
| **Security**               | You choose what data leaves your system            |
| **Validation**             | Apply `@NotBlank`, `@Email`, etc. on DTOs          |
| **Clean API**              | Your API models are separate from database models  |
| **Flexibility**            | Change database structure without breaking the API |
| **Separation of Concerns** | Controllers deal with API data, not DB data        |

---

## * Summary

| Term             | Meaning                              |
|------------------|--------------------------------------|
| **Entity**       | Represents database tables           |
| **Request DTO**  | Data the client sends                |
| **Response DTO** | Data returned to client              |
| **Conversion**   | Entity ↔ DTO (done in service layer) |

DTOs make your API **safer, cleaner, and easier to maintain**.

---

## 📌 A Note on Mapping Libraries

Writing `toDTO()` and `toEntity()` by hand works perfectly well and is great for learning. But in real projects, these mapper classes can get long and repetitive — especially when entities have many fields.

There are libraries that generate this boilerplate code for you automatically:

| Library | How it works |
|---|---|
| **MapStruct** | Generates mapper code at **compile time** via annotations — fast, type-safe, no reflection |
| **ModelMapper** | Maps fields by name at **runtime** using reflection — less code, but harder to debug |
| **Dozer** | XML or annotation-based runtime mapping — older, less commonly used today |
| **JMapper** | Annotation-based, focuses on high performance |

> We will cover **MapStruct** in an upcoming lesson — it is the most popular choice in Spring Boot projects because it has zero runtime overhead and gives clear compile-time errors when a mapping is wrong.
