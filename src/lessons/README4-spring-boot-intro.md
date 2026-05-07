# README – Introduction to Spring Boot

This file explains what Spring Boot is and why developers use it.

Spring Boot is built on top of the Spring Framework.

---

## 1. What problem does Spring Boot solve?

Traditional Spring projects could require a lot of setup:
- many dependencies
- manual configuration
- external application server setup
- boilerplate project creation

**Boilerplate** = repetitive setup code we write in every project, not specific to the problem we are actually solving. Spring Boot generates most of it for us.

Spring Boot reduces this setup work.

It helps developers start faster and focus on application code.

---

## 2. What is Spring Boot?
Spring Boot is a framework that makes it much faster and easier to build backend applications with Java and Spring.

Spring Boot is also an **opinionated framework**.

In software, "opinionated" means the framework has already made decisions for us — so we don't have to.


The opposite of opinionated is a setup where we configure and choose everything from scratch.

In short:

> Spring Boot makes Spring projects easier to create, run, and maintain.

---
## 3. Creating a project

The easiest way is Spring Initializr.

Typical choices:
- Project: Maven
- Language: Java
- Packaging: Jar
- Java version: based on your class setup
- Dependencies: Spring Web, Spring Data JPA, PostgreSQL Driver, DevTools

---

## 4. Basic project structure

``` text
src/
 ├── main/
 │   ├── java/
 │   │   └── com/example/demo/
 │   │       ├── DemoApplication.java
 │   │       ├── controller/
 │   │       ├── service/
 │   │       ├── repository/
 │   │       └── model/
 │   └── resources/
 │       └── application.properties
 └── test/
     └── java/
```

Meaning:
- controller: handles HTTP requests
- service: business logic
- repository: database access
- model: data classes or entities
- resources: configuration files

---
## 5. REST API and the MVC pattern

### What is a REST API?

REST stands for **Representational State Transfer**. It is a way to design web APIs that communicate over HTTP.

The client sends a request to a URL, and the server responds — usually with JSON.

Common REST operations:

| HTTP method | URL | What it usually means |
|---|---|---|
| `GET` | `/students` | return all students |
| `GET` | `/students/1` | return student with id 1 |
| `POST` | `/students` | create a new student |
| `PUT` | `/students/1` | update student with id 1 |
| `DELETE` | `/students/1` | delete student with id 1 |

### 6. What is MVC?

MVC stands for **Model–View–Controller**. It is a pattern for separating concerns in an application.

| Layer | Role | In Spring Boot |
|---|---|---|
| **Model** | The data — entities, DTOs | `model/` package |
| **View** | What the client receives | JSON response (automatic) |
| **Controller** | Receives requests, calls service, returns response | `@RestController` |

In a Spring Boot REST API, the "View" is typically just JSON. We do not need HTML pages unless we add a template engine.

> Request → Controller → Service → Repository → Database

The controller does not know about the database. The repository does not know about HTTP. Each layer has one job.

See [README5-rest-endpoints.md](README5-rest-endpoints.md) for REST naming conventions, controller annotations, and a full working example.

---
## 7. Main class

A Spring Boot project starts from a class like this:

``` java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

### What does `@SpringBootApplication` do?

It combines three ideas:

- `@Configuration`
- `@EnableAutoConfiguration`
- `@ComponentScan`

`@ComponentScan` tells Spring to scan your packages and find classes annotated with `@Component`, `@Service`, `@Repository`, `@Controller`, etc. — those become beans managed by Spring (see [README2-ioc-di.md](README2-ioc-di.md)).

At our level, we can remember this:

> It tells Spring Boot where the application starts.

---

## 8. First endpoint example

``` java
package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public String hello() {
        return "Hello, Spring Boot!";
    }
}
```

When the app runs, visiting `/` returns the text.

---

## 9. Running the application

### In IntelliJ
Run the main class.

### In terminal

``` bash
mvn spring-boot:run
```

Default address:

``` text
http://localhost:8080
```

### Minimal `application.properties`

We usually start with just a few properties:

``` properties
spring.application.name=demo-app
server.port=8080
```

We can change `server.port` if `8080` is busy.

### Profiles (very short intro)

Profiles let us use different settings for different environments (for example `dev` and `prod`).

Example:

``` properties
spring.profiles.active=dev
```

At our level, it is enough to know this exists. We will use it more later.

---

## 10. What we should not misunderstand

### Spring Boot is not a different language
It is still Java.

### Spring Boot is not a replacement for Spring
It is built on top of Spring.

### Spring Boot is not only for microservices
It is also great for small and medium backend projects.

---

## 11. Exercise

Create a project with:
- Spring Web
- DevTools

Then:
1. create a controller
2. add a `GET /hello` endpoint
3. return a simple message

Bonus:
Create `GET /bye` as well.


