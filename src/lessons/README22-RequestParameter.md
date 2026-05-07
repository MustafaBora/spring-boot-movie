# 📥 @RequestParam – Optional Parameters, Search, and PATCH

---

## What is `@RequestParam`?

`@RequestParam` maps a URL query parameter to a method argument.

```
GET /products?category=electronics
```

```java
@GetMapping("/products")
public List<Product> getByCategory(@RequestParam String category) {
    return service.findByCategory(category);
}
```

This works, but the parameter is **required by default**.
If the client does not send it, Spring throws a `400 Bad Request`.

---

## Making Parameters Optional

Add `required = false` to make a parameter optional:

```java
@RequestParam(required = false) String category
```

Or provide a default value — which also makes it optional:

```java
@RequestParam(defaultValue = "0") int page
```

If the client omits the parameter:
- `required = false` → the value is `null`
- `defaultValue = "..."` → the value falls back to what you specified

---

## Real Example: Flexible Product Search

Imagine we have this entity with many fields:

```java
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;
    private String brand;
    private Double minPrice;
    private Double maxPrice;
    private Boolean inStock;

    // getters and setters
}
```

A client searching for products might want to filter by:
- only category
- only brand
- category + price range
- all fields at once
- or nothing at all (return everything)

We do **not** want to force the client to send every field every time.

---

### Controller

```java
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean inStock
    ) {
        List<ProductResponseDTO> results = service.search(name, category, brand, minPrice, maxPrice, inStock);
        return ResponseEntity.ok(results);
    }
}
```

The client can now call:

```
GET /products/search?category=electronics
GET /products/search?brand=Sony&inStock=true
GET /products/search?minPrice=100&maxPrice=500&category=audio
GET /products/search                          ← returns everything
```

All of these are valid. Parameters not sent will be `null` in the service.

---

### Service

In the service, we check each parameter before applying it as a filter:

```java
@Service
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductService(ProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ProductResponseDTO> search(
            String name,
            String category,
            String brand,
            Double minPrice,
            Double maxPrice,
            Boolean inStock
    ) {
        List<Product> all = repository.findAll();

        return all.stream()
                .filter(p -> name == null || p.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(p -> category == null || p.getCategory().equalsIgnoreCase(category))
                .filter(p -> brand == null || p.getBrand().equalsIgnoreCase(brand))
                .filter(p -> minPrice == null || p.getMinPrice() >= minPrice)
                .filter(p -> maxPrice == null || p.getMaxPrice() <= maxPrice)
                .filter(p -> inStock == null || p.getInStock().equals(inStock))
                .map(mapper::toResponseDTO)
                .toList();
    }
}
```

Each filter is only applied when the parameter is **not null**.
If a parameter is null (client did not send it), the filter is skipped entirely.

> For production applications with large datasets, push this filtering into the database
> using the Criteria API or `@Query`, rather than filtering in memory with streams.

---

### Repository (for the database approach)

With Spring Data JPA, we can push the filtering to the database using `@Query`:

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
        select p from Product p
        where (:name is null or lower(p.name) like lower(concat('%', :name, '%')))
        and   (:category is null or lower(p.category) = lower(:category))
        and   (:brand is null or lower(p.brand) = lower(:brand))
        and   (:minPrice is null or p.minPrice >= :minPrice)
        and   (:maxPrice is null or p.maxPrice <= :maxPrice)
        and   (:inStock is null or p.inStock = :inStock)
    """)
    List<Product> search(
            @Param("name") String name,
            @Param("category") String category,
            @Param("brand") String brand,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("inStock") Boolean inStock
    );
}
```

The trick is `:param is null or ...` — if the parameter is null, that condition is skipped at the SQL level.

---

### Repository (Criteria API approach)

The Criteria API builds the query dynamically in Java code.
We only add a predicate for a field when its value is actually present.
This is the most flexible approach — no string-based JPQL, full type safety.

First, the repository must extend `JpaSpecificationExecutor`:

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {
}
```

Then we write a `Specification` that builds predicates on demand:

```java
public class ProductSpecification {

    public static Specification<Product> search(
            String name,
            String category,
            String brand,
            Double minPrice,
            Double maxPrice,
            Boolean inStock
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"));
            }
            if (category != null) {
                predicates.add(cb.equal(cb.lower(root.get("category")),
                        category.toLowerCase()));
            }
            if (brand != null) {
                predicates.add(cb.equal(cb.lower(root.get("brand")),
                        brand.toLowerCase()));
            }
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("minPrice"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("maxPrice"), maxPrice));
            }
            if (inStock != null) {
                predicates.add(cb.equal(root.get("inStock"), inStock));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

Each predicate is only added to the list when the parameter is non-null.
At the end, all collected predicates are combined with `AND`.
If no parameters were sent, `predicates` is empty and the query returns everything.

Update the service to use the specification:

```java
public List<ProductResponseDTO> search(
        String name, String category, String brand,
        Double minPrice, Double maxPrice, Boolean inStock
) {
    Specification<Product> spec = ProductSpecification.search(
            name, category, brand, minPrice, maxPrice, inStock);

    return repository.findAll(spec)
            .stream()
            .map(mapper::toResponseDTO)
            .toList();
}
```

The controller stays exactly the same — it still takes the same `@RequestParam` parameters and passes them to the service.

#### Why Criteria API over `@Query` for dynamic search?

| | `@Query` (JPQL) | Criteria API |
|---|---|---|
| Type safety | ❌ String-based | ✅ Compile-time checked |
| Dynamic predicates | ⚠️ Workaround with `is null or` | ✅ Built for this |
| Readability | ✅ Easy to read | ⚠️ More verbose |
| Best for | Fixed queries | Optional / dynamic filters |

---

## PATCH – Partial Update

`PUT` replaces the entire resource. `PATCH` updates only the fields that are sent.

### The difference

| Method | Behavior | When to use |
|---|---|---|
| `PUT` | Replaces the whole resource | Updating all fields |
| `PATCH` | Updates only the provided fields | Partial update |

### Example: PATCH with a Request DTO

We define a DTO where every field is **optional** (nullable):

```java
public class ProductPatchDTO {

    private String name;
    private String category;
    private String brand;
    private Double minPrice;
    private Double maxPrice;
    private Boolean inStock;

    // getters and setters
}
```

Fields the client does not send will be `null`.

### Controller

```java
@PatchMapping("/{id}")
public ResponseEntity<ProductResponseDTO> patch(
        @PathVariable Long id,
        @RequestBody ProductPatchDTO dto
) {
    ProductResponseDTO updated = service.patch(id, dto);
    return ResponseEntity.ok(updated);
}
```

### Service

```java
public ProductResponseDTO patch(Long id, ProductPatchDTO dto) {
    Product existing = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found: " + id));

    if (dto.getName() != null)     existing.setName(dto.getName());
    if (dto.getCategory() != null) existing.setCategory(dto.getCategory());
    if (dto.getBrand() != null)    existing.setBrand(dto.getBrand());
    if (dto.getMinPrice() != null) existing.setMinPrice(dto.getMinPrice());
    if (dto.getMaxPrice() != null) existing.setMaxPrice(dto.getMaxPrice());
    if (dto.getInStock() != null)  existing.setInStock(dto.getInStock());

    Product saved = repository.save(existing);
    return mapper.toResponseDTO(saved);
}
```

Only fields that are non-null in the DTO are applied.
The rest of the entity stays unchanged.

### Example requests

```
PATCH /products/5
{
  "inStock": false
}
```
→ Only `inStock` is updated. All other fields stay the same.

```
PATCH /products/5
{
  "category": "audio",
  "maxPrice": 299.99
}
```
→ Only `category` and `maxPrice` are updated.

---

## Summary

| Concept | Key Point |
|---|---|
| `@RequestParam(required = false)` | Parameter becomes optional; value is `null` when not sent |
| `@RequestParam(defaultValue = "...")` | Provides a fallback; also makes the parameter optional |
| Search with optional filters | Check `if (param != null)` before applying each filter |
| `@Query` with `is null or` | Push optional filtering into SQL for better performance |
| `PATCH` | Only update fields that are non-null in the request body |
| `PUT` vs `PATCH` | `PUT` replaces everything; `PATCH` touches only what is sent |
