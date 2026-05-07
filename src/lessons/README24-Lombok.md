# 🔧 Lombok – Eliminate Boilerplate

## The Problem: Java is Verbose

Every entity and DTO we write needs the same repetitive code:

```java
public class Book {
    private Long id;
    private String title;
    private String author;
    private Double price;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Book() {}

    public Book(Long id, String title, String author, Double price) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
    }
}
```

This is 30+ lines of code that communicates almost no intent.
Every time we add a field, we update getters, setters, and constructors.

**Lombok** solves this.

---

## What is Lombok?

**Lombok** is an annotation processor that generates boilerplate Java code at **compile time**.

We annotate a class, and Lombok generates the getters, setters, constructors, `equals`, `hashCode`, and `toString` methods before the compiler runs.

Like MapStruct, it is **not a runtime library** — the generated code is plain Java that exists in the compiled output.

---

## ⚙️ Dependency Setup

Add this to `pom.xml`:

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

`<optional>true</optional>` means Lombok is only needed at compile time and is not bundled into the final JAR.

Spring Boot manages the version automatically — no need to specify it manually.

> **IntelliJ IDEA:** Install the **Lombok** plugin (Settings → Plugins → search "Lombok").
> Also enable annotation processing: Settings → Build, Execution, Deployment → Compiler → Annotation Processors → ✅ Enable annotation processing.

---

## Core Annotations

### `@Getter` and `@Setter`

Generates getters and/or setters for all fields:

```java
@Getter
@Setter
public class Book {
    private Long id;
    private String title;
    private String author;
    private Double price;
}
```

This is equivalent to writing all four getters and all four setters by hand.
We can also place `@Getter` or `@Setter` on a single field to generate it for that field only.

---

### `@NoArgsConstructor` and `@AllArgsConstructor`

```java
@NoArgsConstructor   // generates: public Book() {}
@AllArgsConstructor  // generates: public Book(Long id, String title, String author, Double price) {}
public class Book {
    private Long id;
    private String title;
    private String author;
    private Double price;
}
```

JPA requires a no-argument constructor on every `@Entity`.
`@NoArgsConstructor` takes care of that without us writing it.

---

### `@RequiredArgsConstructor`

Generates a constructor only for fields marked `final` or `@NonNull`:

```java
@RequiredArgsConstructor
public class BookService {
    private final BookRepository repository;  // included
    private final BookMapper mapper;          // included
    private int callCount;                    // NOT included (not final)
}
```

This is the **most common Lombok annotation in Spring services**.
Spring uses this constructor for dependency injection — no need for `@Autowired`.

---

### `@ToString`

Generates a readable `toString()` method:

```java
@ToString
public class Book {
    private Long id;
    private String title;
}
// output: Book(id=1, title=Clean Code)
```

We can exclude specific fields:

```java
@ToString(exclude = "password")
```

---

### `@EqualsAndHashCode`

Generates `equals()` and `hashCode()` based on all fields by default:

```java
@EqualsAndHashCode
public class Book {
    private Long id;
    private String title;
}
```

For JPA entities, only use the `id` field to avoid issues with lazy-loaded collections:

```java
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Book {
    @EqualsAndHashCode.Include
    private Long id;
    private String title;
}
```

---

### `@Data`

A shortcut that combines:
- `@Getter`
- `@Setter`
- `@RequiredArgsConstructor`
- `@ToString`
- `@EqualsAndHashCode`

```java
@Data
public class BookRequestDTO {
    private String title;
    private String author;
    private Double price;
}
```

`@Data` is ideal for **DTOs** — simple data carriers with no JPA mapping concerns.

> Avoid `@Data` on `@Entity` classes — the generated `equals`/`hashCode` using all fields
> causes problems with JPA's identity tracking and lazy loading.

---

### `@Builder`

Generates a fluent builder pattern for creating objects:

```java
@Builder
public class Book {
    private Long id;
    private String title;
    private String author;
    private Double price;
}
```

Usage:

```java
Book book = Book.builder()
        .title("Clean Code")
        .author("Robert Martin")
        .price(39.99)
        .build();
```

This is especially useful in tests and when creating objects with many optional fields.

When combining `@Builder` with `@NoArgsConstructor` and `@AllArgsConstructor`, we need all three together — `@Builder` alone generates only an all-args constructor:

```java
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book { ... }
```

---

## Putting it all together — Entity vs DTO

### Entity (with JPA)

```java
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String author;
    private Double price;
}
```

We use individual annotations here instead of `@Data` to avoid JPA-related issues.

`@Data` includes `@EqualsAndHashCode`, which by default uses all fields to compare objects.
For JPA entities this causes two problems:

1. **Lazy-loaded collections** — accessing a collection field inside `equals()` can trigger an unintended database query, sometimes outside an active transaction, causing a `LazyInitializationException`.
2. **Identity confusion** — JPA tracks entities by their database `id`. If we compare two objects using all fields before the `id` is assigned (e.g. a newly created entity that has not been saved yet), two logically identical objects will appear unequal.

Using `@Getter @Setter @NoArgsConstructor @AllArgsConstructor` on an entity keeps us in control — we only generate what we actually need, and we avoid the hidden `equals`/`hashCode` behaviour that `@Data` brings.

### DTO

```java
@Data
public class BookRequestDTO {
    private String title;
    private String author;
    private Double price;
}
```

```java
@Data
public class BookResponseDTO {
    private Long id;
    private String title;
    private String author;
    private Double price;
}
```

`@Data` is safe and concise for DTOs.

---

## Accessing Generated Code While Writing Code

Lombok annotations generate code **at compile time**, so methods like `getId()` or `setTitle()` will never appear in our `.java` source files.
This raises a natural question: how can we call methods that don't exist in the source?

The answer is the **Lombok IntelliJ plugin**.

When the plugin is installed and annotation processing is enabled, IntelliJ reads the Lombok annotations and registers the generated methods in real time — before we even compile.
This means:

- **Autocomplete** suggests the generated getters, setters, and constructors as we type.
- **"Go to Definition"** navigates to the annotated field, not a missing method.
- No red underlines or "cannot resolve method" errors appear.

### Example

We define a class with only annotations and fields:

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private Long id;
    private String title;
    private Double price;
}
```

In another class, we use the generated methods normally:

```java
Book book = new Book();        // @NoArgsConstructor
book.setTitle("Clean Code");   // @Setter — IntelliJ recognises this
book.setPrice(39.99);          // @Setter — no error, shows in autocomplete

String title = book.getTitle(); // @Getter — appears in autocomplete list
```

None of these methods exist in the source file, yet IntelliJ treats them as if they do.

### Seeing exactly what Lombok generates — Delombok

To inspect the full generated code, IntelliJ provides **Delombok**:

> `Code` menu → `Delombok` → select the annotation

Delombok rewrites the source file, replacing each Lombok annotation with the equivalent hand-written Java code it would have produced.
This is useful for understanding what is happening under the hood, or for migrating away from Lombok.

### Other editors (VS Code, Eclipse, etc.)

The real-time method recognition described above is an IntelliJ-specific feature provided by its Lombok plugin.
Other editors handle this differently:

- **VS Code** — install the [**Lombok Annotations Support**](https://marketplace.visualstudio.com/items?itemName=GabrielBB.vscode-lombok) extension. It integrates with the Java Language Server and provides the same autocomplete and error-free experience.
- **Eclipse** — Lombok ships with a dedicated Eclipse installer. Run `java -jar lombok.jar`, point it at the Eclipse installation, and restart. After that, Eclipse recognises all generated methods automatically.
- **Any editor (fallback)** — run `mvn compile` once. After compilation, the generated bytecode is in `target/classes/`. Most language servers will pick up the compiled output and resolve the methods even without a dedicated plugin.

> If your editor still shows errors after installing the plugin, make sure **annotation processing** is enabled in the project settings. Without it, neither the compiler nor the language server will trigger Lombok's code generation.

---

## Summary

| Annotation | Generates |
|---|---|
| `@Getter` / `@Setter` | Getters and/or setters |
| `@NoArgsConstructor` | Empty constructor |
| `@AllArgsConstructor` | Constructor with all fields |
| `@RequiredArgsConstructor` | Constructor for `final` / `@NonNull` fields |
| `@ToString` | `toString()` |
| `@EqualsAndHashCode` | `equals()` and `hashCode()` |
| `@Data` | Getter + Setter + RequiredArgsConstructor + ToString + EqualsAndHashCode |
| `@Builder` | Fluent builder pattern |

| Use case | Recommended annotations |
|---|---|
| JPA `@Entity` | `@Getter @Setter @NoArgsConstructor @AllArgsConstructor` |
| DTO (request / response) | `@Data` |
| Service (dependency injection) | `@RequiredArgsConstructor` |
| Test object creation | `@Builder` |
