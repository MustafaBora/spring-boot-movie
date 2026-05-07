# 🛠️ Developer Tools for Backend Development

Writing backend code is not enough.
Real developers **test, debug, and analyze** their applications using tools.

This file covers the essential tools used in backend development.

---

# 🌐 1. Chrome DevTools (Network Tab)

Even though it looks like a frontend tool, it is **extremely useful for backend debugging**.

## What is it used for?

* Inspect HTTP requests
* Analyze responses
* Debug API issues
* Check status codes

---

## How to open?

* Press `F12` in Chrome
* Go to the **Network** tab
* Refresh the page

---

## What to look at?

### Request

* URL
* HTTP Method (GET, POST, etc.)
* Headers

### Response

* JSON data
* Status code

For what headers and status codes mean, see README0-http.md.

---

## Real usage

👉 “Frontend is not working”
👉 Open DevTools → check request

> Most bugs are found here.

---

# 📘 2. Swagger (OpenAPI)

Swagger is a **live API documentation tool**.

**OpenAPI** is a standard specification for describing REST APIs in a machine-readable format (a JSON or YAML file). Swagger is the toolset that reads that specification and turns it into a visual, interactive UI. In practice, developers often use the two names interchangeably.

---

## What is it used for?

* View API endpoints
* Test endpoints
* Share API documentation

---

## ⚙️ Add to Spring Boot

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

---

## Access

```
http://localhost:8080/swagger-ui.html
```

---

## Why it matters

👉 If Swagger exists, frontend developers don't need to ask us questions 😄

---

# 📮 3. Postman / Insomnia

API testing tools.

---

## What are they used for?

* Send HTTP requests
* Test endpoints
* Send JSON data
* Test authentication

**Authentication** in this context means sending credentials (such as a token or username/password) as part of a request, to verify that the caller is allowed to access the endpoint.

---

## Example

### GET

```
GET http://localhost:8080/api/users
```

### POST

```json
{
  "name": "Kien",
  "email": "kien@test.com"
}
```

---

## Real usage

👉 Test backend without frontend
👉 Useful as a lightweight QA step — **QA (Quality Assurance)** means verifying that an application works correctly before it reaches real users.

---

## Big Picture

* DevTools = observe real traffic
* Postman = simulate client
* Swagger = explore API
* DB tool = inspect truth

---


#  4. Database Tools (DBeaver)

Backend is not only APIs — it also includes **databases**.

---

## What is it used for?

* Connect to database
* Run SQL queries
* Inspect data

---

## Recommended

* DBeaver
* pgAdmin (PostgreSQL)

---

## Real usage

👉 “Why is data wrong?”
👉 Check database directly

---

# 5. Logs (VERY IMPORTANT)

Not a tool — a **core skill**.

---

## Why important?

* Most debugging = reading logs

---

## Example

```
INFO  - Server started
WARN  - Missing value
ERROR - Something failed
```

---

## Real usage

👉 If we don’t read logs, we are blind.

---

# 💻 6. cURL (Terminal Tool)

A simple but powerful tool to send HTTP requests.

---

## Example

```bash
curl http://localhost:8080/api/users
```

---

## Why important?

👉 Makes you independent from UI tools

---

# (Optional) 7. Docker

Used to run applications in **containers**.

A **container** is a lightweight, isolated environment that packages your application together with everything it needs to run (Java version, libraries, config). It works the same way regardless of the machine it runs on — your laptop, a server, or the cloud.

## Why mention it?

👉 Docker is common in real projects and you will encounter it when deploying applications.

---

# Summary

A backend developer:

* Writes code
* Tests APIs
* Debugs requests
* Checks database
* Reads logs

Without these tools, development = blind flying ✈️

---

# Exercises

## Exercise 1

Open DevTools → inspect 3 requests

## Exercise 2

Send a GET request using Postman

## Exercise 3

Add Swagger to your project

## Bonus

Test the same endpoint using:

* DevTools
* Postman
* Swagger
* cURL
