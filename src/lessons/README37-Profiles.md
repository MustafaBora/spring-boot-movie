# Spring Boot Profiles

We touched on profiles briefly in [README4 — Spring Boot Intro](README4-spring-boot-intro.md).  
Here we look at them properly, because we will use them in the next file when we deal with secrets.

---

## The problem profiles solve

A Spring Boot application usually runs in more than one place:

| Environment | What it is |
|---|---|
| **dev** | Our own machine while we are building the app |
| **test** | A separate environment where automated tests run |
| **prod** | The live server where real users connect |

Each environment needs different settings. The database URL on our laptop is not the same as the one on the production server. We do not want to rewrite `application.properties` every time we switch.

**Profiles** let us keep one set of settings per environment and activate the right one at startup.

---

## How it works

Spring Boot reads `application.properties` by default.  
When a profile is active, it also reads `application-{profile}.properties` and **merges** it on top.

Profile-specific values override the defaults. Everything else stays.

### File structure

```
src/main/resources/
 ├── application.properties          ← always loaded (shared defaults)
 ├── application-dev.properties      ← loaded when profile = dev
 └── application-prod.properties     ← loaded when profile = prod
```

### Example

`application.properties` — shared config:

```properties
spring.application.name=my-app
server.port=8080
```

`application-dev.properties` — local database, detailed logging:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/myapp_dev
spring.datasource.username=postgres
spring.datasource.password=devpassword
logging.level.org.springframework=DEBUG
```

`application-prod.properties` — real database, minimal logging:

```properties
spring.datasource.url=jdbc:postgresql://prod-server:5432/myapp
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
logging.level.org.springframework=WARN
```

Notice that in `prod` we use environment variables (`${}`) instead of hardcoded values — we will see exactly why in [README40 — Secrets](README40-Secrets.md).

---

## Activating a profile

### Option 1 — In `application.properties`

```properties
spring.profiles.active=dev
```

This is the most common way during development.

### Option 2 — As a command-line argument

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Useful for running locally in a specific mode without changing the properties file.

### Option 3 — As a JVM argument (when running the jar)

```bash
java -jar myapp.jar --spring.profiles.active=prod
```

This is how production deployments typically activate the profile.

### Option 4 — In IntelliJ IDEA

1. Open **Run > Edit Configurations**
2. Find **Active profiles** (or add `-Dspring.profiles.active=dev` to VM options)
3. Save and run

---

## What gets merged

Suppose the active profile is `dev`:

| Property | Comes from |
|---|---|
| `spring.application.name` | `application.properties` |
| `server.port` | `application.properties` |
| `spring.datasource.url` | `application-dev.properties` (overrides if also in base) |

If a property appears in both files, the **profile-specific file wins**.

---

## The `local` profile — preview for the next file

A common pattern is a `local` profile for developer secrets that should never be committed:

```properties
# application.properties
spring.profiles.active=local
```

```properties
# application-local.properties  ← added to .gitignore
spring.datasource.password=myLocalPassword
```

The local file contains real values and stays off version control entirely.  
We will build on this pattern in [README40 — Secrets](README40-Secrets.md).

---

## `@Profile` — activating beans conditionally

Profiles are not limited to properties files. We can also use them to control which beans Spring registers at all.

The `@Profile` annotation on a class or `@Bean` method tells Spring: **only create this bean when the named profile is active**.

### Example — different email behaviour per environment

During development we do not want to send real emails. We can swap the implementation based on the active profile:

```java
public interface EmailService {
    void send(String to, String subject, String body);
}
```

```java
@Service
@Profile("prod")                         // only active in production
public class SmtpEmailService implements EmailService {

    @Override
    public void send(String to, String subject, String body) {
        // connects to a real SMTP server and sends the email
        System.out.println("Sending real email to " + to);
    }
}
```

```java
@Service
@Profile("dev")                          // only active in development
public class FakeEmailService implements EmailService {

    @Override
    public void send(String to, String subject, String body) {
        // just prints to the console — no real email sent
        System.out.println("[DEV] Would send email to " + to + ": " + subject);
    }
}
```

When `spring.profiles.active=dev`, Spring registers `FakeEmailService` and ignores `SmtpEmailService`.  
When `spring.profiles.active=prod`, the opposite happens.  
The rest of the application injects `EmailService` and never knows which one it got.

### `@Profile` on a `@Bean` method

It works the same way inside a `@Configuration` class:

```java
@Configuration
public class StorageConfig {

    @Bean
    @Profile("dev")
    public StorageService localStorageService() {
        return new LocalDiskStorageService();   // saves files to the local filesystem
    }

    @Bean
    @Profile("prod")
    public StorageService s3StorageService() {
        return new S3StorageService();          // saves files to AWS S3
    }
}
```

### `!` — the "not this profile" shortcut

We can also say "register this bean for every profile **except** prod":

```java
@Service
@Profile("!prod")
public class FakeEmailService implements EmailService { }
```

---

## Summary

| Concept | Meaning |
|---|---|
| Profile | A named set of settings for a specific environment |
| `application-{profile}.properties` | File that is merged on top of the defaults when that profile is active |
| `spring.profiles.active` | Property that tells Spring which profile to use |
| Override rule | Profile-specific values win over base values |
| `@Profile("dev")` | Register this bean only when the `dev` profile is active |
| `@Profile("!prod")` | Register this bean for every profile except `prod` |
| `local` profile | Common name for a gitignored file with developer secrets |

One codebase, multiple environments — no manual file swapping needed.
