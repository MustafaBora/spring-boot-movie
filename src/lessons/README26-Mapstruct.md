# 🗺️ MapStruct – Automatic DTO Mapping

## The Problem: Manual Mapping is Tedious

In the previous section we converted between Entities and DTOs by hand:

```java
public ContactResponseDTO toDTO(Contact c) {
    ContactResponseDTO dto = new ContactResponseDTO();
    dto.setId(c.getId());
    dto.setName(c.getName());
    dto.setPhone(c.getPhone());
    dto.setEmail(c.getEmail());
    return dto;
}
```

This works, but it has problems:

| Problem | Impact |
|---|---|
| Repetitive boilerplate | Every entity needs a manual mapper |
| Easy to forget a field | Silent bugs — field is just `null` |
| Hard to maintain | Adding a field means updating every mapper |

**MapStruct** solves all of this.

---

## What is MapStruct?

**MapStruct** is a code generator that automatically creates the mapping code between Java objects at **compile time**.

We define a simple interface. MapStruct reads it and generates the full implementation.

```
Entity  ──→  MapStruct  ──→  DTO
DTO     ──→  MapStruct  ──→  Entity
```

It is **not a library that runs at runtime** — it generates plain Java code before compilation.
This means zero reflection, zero magic, and excellent performance.

---

## ⚙️ Dependency Setup

Add these two dependencies to `pom.xml`:

```xml
<properties>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>${mapstruct.version}</version>
    </dependency>
</dependencies>
```

Also add the annotation processor to the Maven compiler plugin.

> **If your project also uses Lombok**, you must list **both** processors — and Lombok must come first.
> See the [Lombok + MapStruct](#lombok--mapstruct) section below for the full explanation.

```xml
<properties>
    <lombok.version>1.18.36</lombok.version>
    <mapstruct.version>1.5.5.Final</mapstruct.version>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <!-- Lombok MUST come before MapStruct -->
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                    <path>
                        <groupId>org.mapstruct</groupId>
                        <artifactId>mapstruct-processor</artifactId>
                        <version>${mapstruct.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

> **Why two entries?**
> `mapstruct` provides the annotations we use in our code.
> `mapstruct-processor` is the code generator that runs during compilation and creates the implementation.

---

## Basic Example

### Entity

```java
@Entity
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String phone;
    private String email;

    // getters and setters
}
```

### DTOs

```java
public class ContactRequestDTO {
    private String name;
    private String phone;
    private String email;
    // getters and setters
}

public class ContactResponseDTO {
    private Long id;
    private String name;
    private String phone;
    private String email;
    // getters and setters
}
```

### Mapper Interface

```java
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContactMapper {

    ContactResponseDTO toResponseDTO(Contact contact);

    Contact toEntity(ContactRequestDTO dto);
}
```

That is all. MapStruct reads the field names, matches them automatically, and generates the implementation.

`componentModel = SPRING` tells MapStruct to annotate the generated implementation class with `@Component`, making it a Spring bean that can be injected via `@Autowired` or constructor injection.

> ⚠️ **Do NOT add `@Component` to the mapper interface itself.**
> `componentModel = SPRING` already puts `@Component` on the generated `*Impl` class.
> Adding `@Component` on the interface causes Spring to try to instantiate the interface directly, which fails with:
> `required a bean of type '...Mapper' that could not be found`

---

## How Does MapStruct Match Fields?

By default, MapStruct matches fields by **name**.

If the entity has a field `name` and the DTO has a field `name`, MapStruct connects them automatically.

```
Contact.name  ──→  ContactResponseDTO.name  ✅ (same name, matched)
Contact.email ──→  ContactResponseDTO.email ✅ (same name, matched)
```

---

## Using the Mapper in a Service

```java
@Service
public class ContactService {

    private final ContactRepository repository;
    private final ContactMapper mapper;

    public ContactService(ContactRepository repository, ContactMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public ContactResponseDTO create(ContactRequestDTO dto) {
        Contact entity = mapper.toEntity(dto);
        Contact saved = repository.save(entity);
        return mapper.toResponseDTO(saved);
    }

    public List<ContactResponseDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }
}
```

No manual field copying. No forgotten fields.

---

## Mapping Different Field Names with `@Mapping`

Sometimes the entity and DTO have different field names.

```java
// Entity field: "fullName"
// DTO field: "name"
```

Use `@Mapping` to define the connection explicitly:

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContactMapper {

    @Mapping(source = "fullName", target = "name")
    ContactResponseDTO toResponseDTO(Contact contact);
}
```

`source` is the field on the input object.
`target` is the field on the output object.

---

## Ignoring Fields with `@Mapping(target = "...", ignore = true)`

Sometimes we want to leave a field out of the mapping entirely.

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContactMapper {

    @Mapping(target = "internalNote", ignore = true)
    ContactResponseDTO toResponseDTO(Contact contact);
}
```

`internalNote` will be `null` in the output DTO and will not cause a compile warning.

---

## Mapping a List

MapStruct automatically generates list mappers when we define a single-object mapping.

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContactMapper {

    ContactResponseDTO toResponseDTO(Contact contact);

    List<ContactResponseDTO> toResponseDTOList(List<Contact> contacts);
}
```

We just declare the method — MapStruct applies `toResponseDTO` to each element.

---

## Updating an Existing Entity with `@MappingTarget`

When handling `PUT` requests, we often want to update an existing entity instead of creating a new one.

```java
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ContactMapper {

    void updateEntityFromDTO(ContactRequestDTO dto, @MappingTarget Contact contact);
}
```

Usage in service:

```java
public ContactResponseDTO update(Long id, ContactRequestDTO dto) {
    Contact existing = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Not found"));

    mapper.updateEntityFromDTO(dto, existing);

    Contact saved = repository.save(existing);
    return mapper.toResponseDTO(saved);
}
```

MapStruct copies the fields from `dto` into the existing `contact` object.
The `id` in the entity is untouched because `ContactRequestDTO` does not have an `id` field.

---

## What MapStruct Generates

After compilation, MapStruct creates a class like this in `target/generated-sources`:

```java
@Component
public class ContactMapperImpl implements ContactMapper {

    @Override
    public ContactResponseDTO toResponseDTO(Contact contact) {
        if (contact == null) return null;

        ContactResponseDTO dto = new ContactResponseDTO();
        dto.setId(contact.getId());
        dto.setName(contact.getName());
        dto.setPhone(contact.getPhone());
        dto.setEmail(contact.getEmail());
        return dto;
    }

    @Override
    public Contact toEntity(ContactRequestDTO dto) {
        if (dto == null) return null;

        Contact contact = new Contact();
        contact.setName(dto.getName());
        contact.setPhone(dto.getPhone());
        contact.setEmail(dto.getEmail());
        return contact;
    }
}
```

This is plain, readable Java. No magic at runtime.

---

## Lombok + MapStruct

### Why They Conflict

Both Lombok and MapStruct are **annotation processors** — they both hook into the Java compiler's annotation processing phase. But they depend on each other's output:

| Tool | What it does | When |
|---|---|---|
| **Lombok** | Generates `getters`, `setters`, constructors | Compile time |
| **MapStruct** | Generates mapper code that *calls* those getters/setters | Compile time |

If MapStruct's processor runs **before** Lombok has generated the getters and setters, MapStruct sees a class with no methods and cannot build the mapping code. The result is a mapper that fails to compile or silently maps nothing.

### The Fix: Order Matters

The JVM runs annotation processors in the order they are listed in `annotationProcessorPaths` — so Lombok must come **first**:

```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
    </path>
    <path>                              <!-- must come AFTER Lombok -->
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${mapstruct.version}</version>
    </path>
</annotationProcessorPaths>
```

> **Important:** Lombok's main `<dependency>` uses Spring Boot's BOM to manage its version automatically.
> However, inside `annotationProcessorPaths` the BOM does **not** apply — you must declare `${lombok.version}` explicitly in `<properties>`:
>
> ```xml
> <properties>
>     <lombok.version>1.18.32</lombok.version>
>     <mapstruct.version>1.5.5.Final</mapstruct.version>
> </properties>
> ```

Lombok must be listed **before** MapStruct.


### Troubleshooting

**Common error:** `required a bean of type '...Mapper' that could not be found`

This usually means MapStruct did not generate the `*Impl` class. Check:

1. `maven-compiler-plugin` with `annotationProcessorPaths` is present and **not commented out** in `pom.xml`.
2. `${lombok.version}` is explicitly defined in `<properties>` — the Spring Boot BOM does **not** apply inside `annotationProcessorPaths`.
3. Lombok is listed **before** `mapstruct-processor`.
4. IntelliJ: `Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable annotation processing` is checked.
5. Run `mvn clean compile` and verify `target/generated-sources/annotations/` contains the `*Impl` file.

If getter/setter mapping still fails, add the binding bridge between Lombok and MapStruct:

```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>${lombok.version}</version>
    </path>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok-mapstruct-binding</artifactId>
        <version>0.2.0</version>
    </path>
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>
        <version>${mapstruct.version}</version>
    </path>
</annotationProcessorPaths>
```

---

## Common Annotations Summary

| Annotation | Purpose |
|---|---|
| `@Mapper` | Marks the interface as a MapStruct mapper |
| `componentModel = SPRING` | Makes the mapper a Spring bean (`@Autowired`-able) |
| `@Mapping(source, target)` | Maps a field with a different name |
| `@Mapping(target, ignore = true)` | Excludes a field from mapping |
| `@MappingTarget` | Marks the parameter to update in place (for `PUT`) |

---

## Manual Mapping vs MapStruct

| | Manual | MapStruct |
|---|---|---|
| Boilerplate | High | None |
| Compile-time safety | No | Yes |
| Missing field detection | Silent `null` | Compile warning |
| Performance | Same | Same (plain Java) |
| Readable generated code | You write it | MapStruct generates it |
| Works with Spring | Yes | Yes (`componentModel = SPRING`) |

---

## Summary

- **MapStruct** generates DTO ↔ Entity mapping code at compile time
- We define a `@Mapper` interface with method signatures — MapStruct writes the implementation
- Fields with the same name are matched automatically
- Use `@Mapping` to handle different field names or to ignore fields
- Use `@MappingTarget` to update an existing entity in a `PUT` scenario
- The generated code is plain Java — no reflection, no runtime overhead
