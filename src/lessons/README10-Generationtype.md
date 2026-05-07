# **JPA `GenerationType` Cheat Sheet**

In JPA, the `@GeneratedValue` annotation defines how the primary key for an entity is automatically generated. The `strategy`
attribute specifies the `GenerationType` to use.

---

## **1. `AUTO`**

```java
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
private Long id;
```

* **Behavior:** JPA automatically selects the generation strategy based on the underlying database dialect.
* **Usage:** Ideal when we want JPA to determine the most appropriate strategy for our database.
* **Example:** On MySQL, it may use `IDENTITY`; on Oracle, it may use a sequence.

---

## **2. `IDENTITY`**

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

* **Behavior:** The database automatically generates the primary key, typically using an auto-increment column.
* **Usage:** Best suited for databases that support auto-increment columns, such as MySQL and SQL Server.
* **Note:** The ID is generated **at insert time**, so the entity must be persisted immediately to obtain the generated ID.

---

## **3. `SEQUENCE`**

```java
@Id
@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
@SequenceGenerator(name = "my_seq", sequenceName = "MY_SEQ", allocationSize = 1)
private Long id;
```

* **Behavior:** Uses a database sequence to generate unique primary key values.
* **Usage:** Recommended for databases that support sequences, including Oracle, PostgreSQL, and DB2.
* **Key Attributes:**

  * `sequenceName` → the name of the database sequence.
  * `allocationSize` → the number of IDs to preallocate (default is 50).

---

## **4. `TABLE`**

```java
@Id
@GeneratedValue(strategy = GenerationType.TABLE, generator = "my_table_gen")
@TableGenerator(
    name = "my_table_gen",
    table = "ID_GEN",
    pkColumnName = "GEN_NAME",
    valueColumnName = "GEN_VAL",
    allocationSize = 1
)
private Long id;
```

* **Behavior:** Uses a dedicated table to simulate sequence generation.
* **Usage:** Suitable for cross-database compatibility, even if the database doesn’t support sequences.
* **Note:** Less efficient than `IDENTITY` or `SEQUENCE` due to additional table lookups.

---

## **Quick Comparison**

| Strategy   | Database Dependent                   | ID Generation Method      | Recommended Use Case       |
|------------|--------------------------------------|---------------------------|----------------------------|
| `AUTO`     | Yes                                  | JPA selects automatically | Default for portability    |
| `IDENTITY` | No (requires auto-increment support) | Database auto-increment   | MySQL, SQL Server          |
| `SEQUENCE` | Yes                                  | Database sequence         | Oracle, PostgreSQL         |
| `TABLE`    | No                                   | Table-based ID generation | Cross-database portability |

---

## **Rule of Thumb**

* Prefer **`SEQUENCE`** if our database supports sequences.
* Use **`IDENTITY`** for simple auto-increment primary keys.
* Choose **`AUTO`** or **`TABLE`** for database-independent solutions.

---
