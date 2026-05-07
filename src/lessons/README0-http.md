# README – HTTP and the Web

Before writing any Spring Boot code, it helps to understand what happens under the hood when two computers talk to each other over the internet.

---

## 1. What happens when you type a URL?

You type `https://www.google.com` into your browser and press Enter.

What happens:

1. Your browser (**client**) looks up the address and finds the server.
2. The browser sends an **HTTP request** to that server.
3. The server receives the request, processes it, and sends back an **HTTP response**.
4. The browser reads the response and shows you the page.

This is the **client–server model**. It is the foundation of the web.

```
Client (browser)           Server (google.com)
      |                           |
      |  --- HTTP Request --->    |
      |                           |
      |  <-- HTTP Response ---    |
      |                           |
```

In a Spring Boot app, your Java application is the **server**. Postman, a browser, or a frontend app is the **client**.

---

## 2. What is HTTP?

**HTTP** stands for HyperText Transfer Protocol.

It is a set of rules for how a client sends requests and a server sends back responses. Both sides speak the same language.

HTTP is text-based. A request and a response are just structured text sent over a network connection.

**HTTPS** is the same thing but encrypted. The `S` stands for Secure.

---

## 3. URL anatomy

A URL like `https://api.example.com:8080/users?page=2` has several parts:

```
https   ://   api.example.com   :8080   /users   ?page=2
  |             |                 |        |          |
protocol       host              port     path     query string
```

| Part | What it is |
|---|---|
| `https` | The protocol — how the data is transferred |
| `api.example.com` | The host — which server to talk to |
| `8080` | The port — which door to knock on (more below) |
| `/users` | The path — which resource you want |
| `?page=2` | Query string — extra parameters passed to the server |

---

## 4. What is a port?

A server can run many programs at once. A **port** is a number that identifies which program on that server should receive the request.

Common defaults:

| Port | Used by |
|---|---|
| `80` | HTTP |
| `443` | HTTPS |
| `8080` | Local development servers (Spring Boot default) |
| `5432` | PostgreSQL |

When we run a Spring Boot app locally, it listens on port `8080` by default. That is why we access it at `http://localhost:8080`.

`localhost` means "this machine" — we are both client and server during development.

---

## 5. HTTP request structure

Every HTTP request has:

```
POST /api/users HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "name": "Lizhen",
  "email": "lizhen@example.com"
}
```

| Part | Description |
|---|---|
| `POST` | The HTTP method — what you want to do |
| `/api/users` | The path — which resource you are targeting |
| `Host`, `Content-Type` | Headers — metadata about the request |
| `{ ... }` | Body — the data you are sending (not all requests have a body) |

## Try it now

Open browser:
http://localhost:8080

Then:
- open DevTools
- go to Network
- refresh

Find one request and inspect it. See the method, headers, and body.

### HTTP methods

| Method | Typical use |
|---|---|
| `GET` | Read data — no body |
| `POST` | Create a new resource — has a body |
| `PUT` | Replace an existing resource — has a body |
| `PATCH` | Partially update a resource — has a body |
| `DELETE` | Remove a resource — usually no body |

---

## 6. HTTP response structure

Every HTTP response has:

```
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": 1,
  "name": "Lizhen",
  "email": "lizhen@example.com"
}
```

| Part | Description |
|---|---|
| `201` | Status code — the result of the request |
| `Content-Type` | Header — tells the client what format the body is in |
| `{ ... }` | Body — the data the server returns |

### Status codes

| Code | Meaning |
|---|---|
| `200` | OK — success |
| `201` | Created — new resource was created |
| `204` | No Content — success but no body |
| `400` | Bad Request — something is wrong with the input |
| `401` | Unauthorized — not authenticated |
| `403` | Forbidden — authenticated but not allowed |
| `404` | Not Found — the resource does not exist |
| `500` | Internal Server Error — something went wrong on the server |

---

## 7. What is a header?

**Headers** are key-value pairs sent alongside a request or response. They carry metadata — information about the message, not the message itself.

Common request headers:

| Header | Example value | What it means |
|---|---|---|
| `Content-Type` | `application/json` | The body is JSON |
| `Authorization` | `Bearer <token>` | JWT or API key for authentication |
| `Accept` | `application/json` | The client wants JSON back |

`Content-Type: application/json` is the most important one to know right now. When we send JSON in a POST request body, we must include this header so the server knows how to read it.

---

## 8. HTTP is stateless

Each HTTP request is **independent**. The server does not remember the previous request.

If we call `GET /users/1` and then `DELETE /users/1`, the server handles those as two completely separate conversations. It does not know they came from the same client unless we explicitly include something in the request (like a token) that identifies us.

This is a design choice, not a limitation. It keeps servers simpler and easier to scale.

---

## 9. What is JSON?

**JSON** stands for JavaScript Object Notation.

It is a text format for representing structured data. It was originally used in JavaScript but is now the standard format for APIs regardless of programming language.

```json
{
  "id": 1,
  "name": "Lizhen",
  "email": "lizhen@example.com"
}
```

Rules:
- keys are always strings in double quotes
- values can be: string, number, boolean, array, object, or null
- no trailing commas

An array:

```json
[
  { "id": 1, "name": "Gomathi" },
  { "id": 2, "name": "Preeti" }
]
```

### Why JSON and not something else?

Before JSON, XML was common:

```xml
<user>
  <id>1</id>
  <name>Lizhen</name>
  <email>lizhen@example.com</email>
</user>
```

JSON won because it is shorter, easier to read, and easier to work with in code.

### JSON and Java

Java does not understand JSON natively. When a Spring Boot endpoint receives a JSON body, it has to convert that JSON into a Java object. When it returns a Java object, it has to convert it back to JSON.

This conversion is called **serialization** (Java → JSON) and **deserialization** (JSON → Java). Spring Boot handles this automatically using a library called **Jackson**. We do not need to write any conversion code.

---

## 10. Quick summary

| Term | Short meaning |
|---|---|
| HTTP | Protocol for client–server communication |
| HTTPS | HTTP with encryption |
| URL | Address of a resource |
| Port | Door number on a server |
| Request | Message from client to server |
| Response | Message from server to client |
| Header | Metadata attached to a request or response |
| Status code | Number indicating the result |
| Stateless | Each request is independent |
| JSON | Text format for structured data |
| Serialization | Converting a Java object to JSON (or other format) |
| Deserialization | Converting JSON back to a Java object |
