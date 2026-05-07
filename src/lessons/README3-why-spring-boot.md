# README – Why Spring Boot Instead of Plain Java?

This file compares a plain Java backend project with a Spring Boot project.

The goal is not to say plain Java is useless.  
The goal is to understand why Spring Boot is popular in real backend development.

---

## 1. Plain Java project

A plain Java project can absolutely be useful for learning the fundamentals.

But when building a real backend application, plain Java often means more manual work.

---

## 2. Main differences

## Setup

### Plain Java
More things need to be built manually:
- project wiring
- library setup
- configuration decisions

### Spring Boot
We start faster with starter dependencies and default conventions.

In practice, many teams begin with Spring Initializr to create the project skeleton quickly.

---

## Server

### Plain Java
To serve HTTP requests properly, more setup or extra libraries are usually needed.

### Spring Boot
A web application can run with an embedded server immediately.

**Tomcat** is a web server — it listens on a port (like 8080), accepts incoming HTTP requests, and hands them to your Java application.

Normally we would install and configure Tomcat as a separate program. Spring Boot bundles it inside our application.
When we run the app, the server starts automatically with it.

---

## Dependency management

### Spring Boot
Starter dependencies group common libraries together.

The Spring Boot part provides starter dependencies and version management conventions on top of Maven or Gradle.

Example:

``` xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### Parent in `pom.xml`

In most Spring Boot projects, `pom.xml` has this parent:

``` xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.4</version>
    <relativePath/>
</parent>
```

Why this helps:

- many dependency versions are managed for us
- plugin versions are managed too
- your `pom.xml` stays cleaner

So for most dependencies, we do not need to write a version number manually.

---

## Object management

### Plain Java
Objects are created manually with `new`.

### Spring Boot
Spring manages beans and injects dependencies.

---

## Configuration

### Plain Java
A lot of choices are manual.

### Spring Boot
Many common settings are auto-configured.

Example:
if we add `spring-boot-starter-web`, Spring Boot can automatically prepare common web setup such as Spring MVC and JSON handling.

> Convention over configuration: sensible defaults are provided, and we override only what we need.

---

## Testing support

### Plain Java
Test tools need to be added and configured manually.

### Spring Boot
Testing support is integrated nicely with starter dependencies.

We will also often see annotations such as `@SpringBootTest` for integration-style tests.

---

## Production features

### Plain Java
Extra setup or extra libraries are often needed for things like health checks and metrics.

### Spring Boot
Spring Boot can provide production-oriented tools through Actuator.

Examples:

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`

This is useful later when applications grow and need monitoring.

---

## Microservices direction

### Plain Java
Microservices can be built with plain Java, but more pieces need to be assembled manually.

### Spring Boot
Spring Boot is often chosen for microservices because it starts quickly, works well with REST APIs, and fits well with the wider Spring ecosystem.

That does not mean Spring Boot is only for microservices.
It is also useful for normal backend applications.

---

## 3. Simple comparison table

| Topic | Plain Java | Spring Boot |
| --- | --- | --- |
| Setup speed | slower | faster |
| Server setup | more manual | embedded server |
| Dependency wiring | manual | DI with Spring |
| Configuration | mostly manual | many defaults |
| Defaults | you decide most things yourself | opinionated defaults |
| REST API creation | more effort | very easy |
| Testing support | more manual setup | starter support and `@SpringBootTest` |
| Production tools | add more manually | actuator support |
| Microservices | more manual assembly | common and convenient choice |
| Project conventions | fully manual | standard structure |

---

## 4. Why teams like Spring Boot

Teams often prefer Spring Boot because it gives:
- faster onboarding
- similar project structure across teams
- easier maintenance
- easier deployment
- strong ecosystem support

This becomes more important as projects grow.

---

## 5. Quick summary

Spring Boot is popular because it reduces manual backend setup.

It gives:
- conventions
- auto-configuration
- embedded server
- easier REST development
- faster project startup

Plain Java is still valuable for learning fundamentals.
