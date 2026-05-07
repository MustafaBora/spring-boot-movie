# 🔧 Advanced Bean Configuration

This file covers advanced Spring bean configuration topics that build on the basics in [README2 — IoC & DI](README2-ioc-di.md).

---

## When do you need `@Bean` explicitly?

Most of the time Spring finds your classes automatically through component scanning (`@Component`, `@Service`, etc.).  
But there are situations where you **must** use `@Bean` explicitly:

**Case 1 — The class comes from an external library**

You cannot add `@Component` to a class you don't own (e.g. a third-party SDK or a class from the JDK).  
`@Bean` lets you register it anyway:

```java
@Configuration
public class AppConfig {

    // ObjectMapper is a Jackson class — we don't own its source code
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;          // now injectable anywhere with @Autowired
    }
}
```

**Case 2 — You need multiple instances of the same type with different configuration**

Component scanning creates one bean per class. If you need two differently configured instances of the same class, `@Bean` with distinct names is the only way:

> **What is `DataSource`?**
>
> When we write `UserRepository extends JpaRepository`, Spring Data handles everything for us — we never think about how the connection to the database is opened or managed.
> Under the hood, something has to actually connect to the database: know the URL, the username, the password, and manage a pool of open connections so the app does not open a new one on every request.
> That something is `DataSource`. It is a standard Java interface (`javax.sql.DataSource`) that represents the **connection pool** — the bridge between our application and the database.
>
> The full picture looks like this:
> ```
> DataSource        ← connection pool (e.g. HikariCP)
>     ↓
> EntityManager     ← JPA layer, runs SQL through the DataSource
>     ↓
> Repository        ← what we write
> ```
>
> In a normal Spring Boot app, `DataSource` is configured automatically from `application.properties` and we never touch it.
> It becomes relevant here because it is a perfect example of a bean we cannot annotate ourselves (it comes from the JDK), and a real-world case where we might genuinely need **two** — one pointing to the main database and one to a read-only reporting replica.

```java
@Configuration
public class DataSourceConfig {

    @Bean(name = "primaryDataSource")
    public DataSource primaryDataSource() {
        // points to the main database
        return DataSourceBuilder.create().url("jdbc:mysql://main-db/app").build();
    }

    @Bean(name = "reportingDataSource")
    public DataSource reportingDataSource() {
        // points to a read-only replica for reports
        return DataSourceBuilder.create().url("jdbc:mysql://replica-db/app").build();
    }
}
```

With component scanning alone there would be no way to tell Spring "create two separate `DataSource` beans from the same class".

---

## Injecting `@Bean` beans into services

Once a bean is registered (via `@Bean` or `@Component`), you inject it the same way as any other bean — with constructor injection. Spring matches by **type** by default.

**Single bean of that type — just inject it normally:**

```java
@Service
public class NotificationService {

    private final ObjectMapper objectMapper;  // Spring finds the one ObjectMapper bean

    public NotificationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
```

**Multiple beans of the same type — use `@Qualifier` to pick one by name:**

When you have `primaryDataSource` and `reportingDataSource`, Spring does not know which one to inject and will throw an error unless you tell it explicitly.

```java
@Service
public class OrderService {

    private final DataSource dataSource;

    public OrderService(@Qualifier("primaryDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
```

```java
@Service
public class ReportService {

    private final DataSource dataSource;

    public ReportService(@Qualifier("reportingDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
```

**Alternative — mark one bean as `@Primary`:**

If one bean should be the default choice and the other is the exception, annotate the default with `@Primary`. Spring uses it automatically unless overridden with `@Qualifier`.

```java
@Configuration
public class DataSourceConfig {

    @Bean(name = "primaryDataSource")
    @Primary                              // used when no @Qualifier is specified
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().url("jdbc:mysql://main-db/app").build();
    }

    @Bean(name = "reportingDataSource")
    public DataSource reportingDataSource() {
        return DataSourceBuilder.create().url("jdbc:mysql://replica-db/app").build();
    }
}
```

```java
@Service
public class OrderService {
    // no @Qualifier needed — picks primaryDataSource automatically
    public OrderService(DataSource dataSource) { }
}

@Service
public class ReportService {
    // explicitly requests the other one
    public ReportService(@Qualifier("reportingDataSource") DataSource dataSource) { }
}
```

| Situation | Solution |
|---|---|
| One bean of that type | Just inject — Spring finds it automatically |
| Multiple beans, one is the main default | `@Primary` on the default, `@Qualifier` on the exception |
| Multiple beans, no clear default | `@Qualifier` on every injection point |

---

## What Is ApplicationContext?

`ApplicationContext` is the **central Spring container** — the place where all beans live.  
Think of it as a smart registry: it creates beans, wires their dependencies, and hands them out when needed.

### Using ApplicationContext directly (rarely needed)

```java
@Service
public class ReportService {

    // Spring injects the context itself
    @Autowired
    private ApplicationContext context;

    public void generateReport() {
        // fetch a prototype bean manually
        ReportBuilder builder = context.getBean(ReportBuilder.class);
        builder.addLine("Data");
        System.out.println(builder.build());
    }
}
```

In Spring Boot you almost never interact with `ApplicationContext` directly — Spring wires everything for you.  
You need it mainly when you must **retrieve a prototype bean at runtime** (see Bean Scopes below).

---

## Bean Scopes

Every object managed by the Spring container is called a **bean**.  
A **bean scope** defines **how many instances** of that bean Spring creates and **how long** each instance lives.

| Scope         | Instances created | Lifetime                                  | Typical use                        |
|---------------|-------------------|-------------------------------------------|------------------------------------|
| `singleton`   | **One** per container | Lives as long as the application runs | Services, repositories (stateless) |
| `prototype`   | **New one** every time it is requested | Destroyed after use | Stateful helper objects            |
| `request`     | **One per HTTP request** | Lives for one web request             | Web-layer objects (Spring MVC)     |
| `session`     | **One per HTTP session** | Lives for the browser session         | User-session data (Spring MVC)     |

---

### Singleton (default)

Spring creates **one shared instance** for the whole application context.  
This is the default — you do not need to write anything extra.

```java
@Service   // singleton by default
public class ProductService {
    // one shared instance used everywhere
}
```

You can also declare it explicitly:

```java
@Component
@Scope("singleton")
public class ProductService { }
```

> Use singleton for **stateless** beans: services, repositories, utilities.

#### Should singleton bean classes be injected as `final`?

What **should** be `final` are the **injected fields** (the dependencies inside a bean):

```java
@Service
public class OrderService {

    private final PaymentService paymentService;   // final field — good practice

    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

Making injected fields `final`:
- guarantees they are set **once** (at construction) and never accidentally reassigned
- enables immutability and makes the class easier to reason about
- works perfectly with Spring because Spring sets them through the constructor

---

### Prototype

Spring creates a **brand-new instance** every time the bean is requested.

```java
@Component
@Scope("prototype")
public class ShoppingCart {
    private List<String> items = new ArrayList<>();

    public void addItem(String item) { items.add(item); }
    public List<String> getItems()   { return items;    }
}
```

Another real-world example — a CSV exporter that accumulates rows before writing:

```java
@Component
@Scope("prototype")
public class CsvExporter {
    private final StringBuilder buffer = new StringBuilder();

    public void addRow(String... columns) {
        buffer.append(String.join(",", columns)).append("\n");
    }

    public String export() {
        return buffer.toString();
    }
}
```

Each export job needs its own buffer — if `CsvExporter` were a singleton, concurrent requests would mix each other's rows together. Prototype guarantees every caller gets a clean, independent instance.

### Request and Session scopes (web apps only)

These scopes only make sense inside a running web application because their **lifetime is tied to the HTTP lifecycle** — a request scope bean is created when an HTTP request arrives and discarded when the response is sent; a session scope bean is created when a user's browser session starts and destroyed when it ends. Outside a web context (e.g. in a batch job or a unit test) there is no request or session, so Spring has nowhere to bind these beans.

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext {
    private String traceId = UUID.randomUUID().toString();
    public String getTraceId() { return traceId; }
}
```

```java
@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserSession {
    private String loggedInUser;
    // getters / setters
}
```

> **Note on session scope in modern applications**  
> Session scope is rarely used today. Most modern REST APIs are **stateless** — the server stores nothing between requests.  
> Authentication is handled with **JWT (JSON Web Token)**: the client sends a signed token with every request, and the server verifies it without needing to remember who the user is in memory.  
> This makes applications easier to scale (any server can handle any request) and removes the need for server-side session storage entirely.  
> You will still see session scope in older server-rendered web apps (JSP, Thymeleaf with full sessions), but in a typical Spring Boot REST API you will not need it.

---

### Scoped Proxy (Advanced / Optional)

A **scoped proxy** solves the problem of injecting a shorter-lived bean (e.g. prototype or request) into a longer-lived one (e.g. singleton).

Without a proxy, Spring injects the dependency **once at startup** and never refreshes it.  
With `proxyMode = ScopedProxyMode.TARGET_CLASS`, Spring injects a **proxy object** that fetches the real bean on every method call.

```java
@Component
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ReportBuilder {
    private final List<String> lines = new ArrayList<>();

    public void addLine(String line) { lines.add(line); }
    public String build()            { return String.join("\n", lines); }
}
```

```java
@Service
public class ReportService {

    // Spring injects a proxy — each call to reportBuilder delegates
    // to a fresh ReportBuilder instance
    @Autowired
    private ReportBuilder reportBuilder;

    public String generateReport() {
        reportBuilder.addLine("Header");
        reportBuilder.addLine("Content");
        return reportBuilder.build();
    }
}
```

> `ScopedProxyMode.TARGET_CLASS` uses CGLIB to subclass the bean.  
> `ScopedProxyMode.INTERFACES` uses a JDK dynamic proxy (requires an interface).

---
