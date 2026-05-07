# 🧠 Inversion of Control (IoC) & Dependency Injection (DI)

At the heart of the **Spring Framework** are two fundamental principles:  
**Inversion of Control (IoC)** and **Dependency Injection (DI)**.  
These concepts allow Spring to manage the lifecycle and relationships between objects, making applications more modular, maintainable, and testable.

---

## ⚙️ What Is Inversion of Control (IoC)?

In a traditional Java application, developers are responsible for creating and managing objects.  
This leads to **tight coupling** between classes, which makes testing and scaling difficult.

**Inversion of Control** means that the control of object creation and dependency management is **inverted** — transferred
from the developer to the **Spring container**.

### 💡 In Simple Terms:
> Instead of our code controlling objects, Spring controls our dependencies.

---

### 🧩 Example: Without IoC
```java
public class UserService {
    private UserRepository userRepository = new UserRepository();
}
```
Here, `UserService` **creates** its own dependency (`UserRepository`).  
This makes it difficult to replace or test `UserRepository` independently.

---

### ✅ Example: With IoC
```java
@Service
public class UserService {

    private final UserRepository userRepository;

    // Dependency is provided, not created
    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```
Now, the **Spring IoC container** creates and injects the `UserRepository` object.  
`UserService` doesn’t need to know *how* it’s created — it just uses it.

---

## What Is a Bean?

A **bean** is simply any Java object that is **created and managed by the Spring container**.  
When we annotate a class with `@Component`, `@Service`, `@Repository`, or `@Controller`, Spring picks it up, instantiates it, and stores it in its internal registry — making it available for injection anywhere in the application.

```java
@Service                      // tells Spring: "manage this object as a bean"
public class EmailService {

    public void send(String to, String message) {
        System.out.println("Sending to " + to + ": " + message);
    }
}
```

We can also declare beans explicitly in a configuration class using `@Bean`:

```java
@Configuration
public class AppConfig {

    @Bean                     // the return value is registered as a bean
    public EmailService emailService() {
        return new EmailService();
    }
}
```

> Summary: every object Spring creates is a bean. Every bean is stored in the **ApplicationContext**.

> Advanced topics — `@Bean` with external libraries, `@Qualifier`, `@Primary`, and `ApplicationContext` — are covered in [README29-Beans-Advanced.md](README29-Beans-Advanced.md).

---

## How IoC Works in Spring

1. **Spring Container**: The core of the framework that manages application objects (called *beans*).  
2. **Configuration Metadata**: Tells Spring which components to create — via annotations, XML, or Java config.  
3. **Bean Lifecycle**: Spring instantiates, configures, and wires beans together automatically.  

The result?  
Your classes are loosely coupled, easier to test, and follow clean architecture principles.

---

##  What Is Dependency Injection (DI)?

**Dependency Injection (DI)** is a design pattern used by Spring to implement IoC.  
It means that instead of a class creating its own dependencies, **they are injected** by an external entity (the Spring container).

### Types of Dependency Injection in Spring
| Type                      | Description                                             | Example                          |
|---------------------------|---------------------------------------------------------|----------------------------------|
| **Constructor Injection** | Dependencies are provided through the class constructor | ✅ Recommended approach           |
| **Setter Injection**      | Dependencies are set via setter methods                 | Useful for optional dependencies |
| **Field Injection**       | Dependencies are injected directly into fields          | Quick, but less testable         |

---

###  Example: Constructor Injection (Preferred)
```java
@Component
public class OrderService {

    private final PaymentService paymentService;

    @Autowired
    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void processOrder() {
        paymentService.pay();
    }
}
```

### Example: Setter Injection
```java
@Component
public class OrderService {

    private PaymentService paymentService;

    @Autowired
    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

### Example: Field Injection
```java
@Component
public class OrderService {

    @Autowired
    private PaymentService paymentService;

    public void processOrder() {
        paymentService.pay();
    }
}
```
It works, but it is usually not preferred.

Why:
- harder to test
- dependency is less explicit
- not ideal for clean design

---

## Common annotations

### `@Component`
Marks a class as a Spring-managed bean.

### `@Service`
Used for service layer classes.

### `@Repository`
Used for data access classes.

### `@Controller`
Used for MVC controllers.

### `@RestController`
Used for REST APIs. Returns JSON by default.

### `@Autowired`
Tells Spring to inject a dependency.

Note:
If a class has only one constructor, Spring can inject it without writing `@Autowired`.

Example:

``` java
@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }
}
```

This is valid in modern Spring Boot.

When is `@Autowired` required?

When a class has **more than one constructor**, Spring does not know which one to use. We must mark the intended one with `@Autowired` so Spring picks the right constructor:

```java
@Service
public class ReportService {

    private final DataSource dataSource;
    private final EmailService emailService;

    // Spring uses this constructor for injection
    @Autowired
    public ReportService(DataSource dataSource, EmailService emailService) {
        this.dataSource = dataSource;
        this.emailService = emailService;
    }

    // This constructor is for testing — we pass mocks manually here
    public ReportService(DataSource dataSource) {
        this.dataSource = dataSource;
        this.emailService = null;
    }
}
```

Without `@Autowired` in this case, Spring throws a `NoUniqueBeanDefinitionException` at startup because it cannot decide which constructor to call.

> Bean scopes (singleton, prototype, request, session) and scoped proxies are covered in [README29-Beans-Advanced.md](README29-Beans-Advanced.md).

---

## Why IoC & DI Matter

- Promotes **loose coupling** between components  
- Makes **testing easier** (mock dependencies easily)  
- Improves **code reusability** and **readability**  
- Simplifies **application maintenance** and **scalability**

---

## Real-Life Analogy

Think of a **coffee machine**:

Without IoC:
- We must buy coffee beans, fill the tank, and handle everything ourselves.

With IoC:
- Someone else (Spring) takes care of preparing the coffee — we just press the button!

---

## Mini testing example
Without DI:

``` java
public class OrderService {

    private PaymentService paymentService = new PaymentService();

}
```

Harder to test because you cannot easily replace `PaymentService`.

With DI:

``` java
public class OrderService {

    private final PaymentService paymentService;

    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

}
```

## Rule

If we see `new` inside a service → something is wrong

---

## Do we ever write `new`?

Yes — but only for objects that are **not beans**: simple data holders, value objects, and things that belong to *your* logic rather than Spring's infrastructure.

**`new` is fine for:**

```java
// A plain data container — no reason to make this a bean
List<String> names = new ArrayList<>();

// A value object you create and immediately use
LocalDate deadline = new LocalDate(2026, 12, 31);

// A DTO you build inside a service method and return
public StudentDTO toDTO(Student student) {
    return new StudentDTO(student.getId(), student.getName()); // fine
}

// An exception
throw new IllegalArgumentException("Invalid input");
```

**`new` is wrong for:**

```java
@Service
public class OrderService {
    // BAD — Spring cannot manage, intercept, or test this
    private PaymentService paymentService = new PaymentService();
}
```

The rule of thumb:  
> If the object **does work** (calls a database, sends an email, has business logic) → it should be a Spring bean, injected by the container.  
> If the object **holds data** (a DTO, a list, a value) → create it with `new` wherever we need it.

---

## Exercise

**Part 1 — Refactor to constructor injection**

Refactor this code so that `SmsService` is injected by Spring instead of created with `new`:

```java
public class NotificationService {

    private SmsService smsService = new SmsService();

}
```

Then answer:
1. What changed?
2. Why is the new version better?
3. Which object should create the dependency now?

---

**Part 2 — Know when `new` is right**

Extend the refactored `NotificationService` so that it:
- still receives `SmsService` via constructor injection
- keeps an internal `List<String>` log of all sent messages (created with `new` inside the constructor)
- has a `send(String to, String message)` method that sends via `SmsService` and adds an entry to the log
- has a `getLogs()` method that returns the log list

Expected result:

```java
@Service
public class NotificationService {

    private final SmsService smsService;   // injected by Spring
    private final List<String> log;        // owned by this class

    public NotificationService(SmsService smsService) {
        this.smsService = smsService;
        this.log = new ArrayList<>();      // new is fine here — this is our own data
    }

    public void send(String to, String message) {
        smsService.send(to, message);
        log.add("Sent to " + to + ": " + message);
    }

    public List<String> getLogs() {
        return log;
    }
}
```

Then answer:
1. Why is `new ArrayList<>()` acceptable here but `new SmsService()` was not?
2. Why can both fields be `final`?

---

## Summary

- IoC means Spring manages object creation
- DI means dependencies are provided from outside
- Constructor injection is the preferred style
- This leads to loose coupling and easier testing
---

