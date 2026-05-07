# Secrets Management

## 🔐 What is a Secret?

A **secret** is any sensitive piece of information your application needs to function, but that should **never be visible to others**:

| Examples |
|---|
| Database passwords |
| API keys |
| JWT signing secrets |
| Third-party service credentials (Stripe, Twilio, etc.) |

---

## ❌ The Problem — Secrets in Code

It is tempting to write this:

```properties
spring.datasource.password=mypassword123
```

and commit it to GitHub. **This is dangerous.** Once a secret is pushed to a public (or even private) repository:

- It is stored in **git history forever**, even if you delete it later
- Bots actively scan GitHub for leaked passwords and API keys
- Anyone with access to the repo can read it

> **Rule: Never commit a real secret to version control.**

---

## Solution — Local Overrides with a Gitignored File

Create a second properties file specifically for local secrets:

```properties
# application-local.properties  ← this file is gitignored
spring.datasource.password=yourpassword
```

Activate it in your main `application.properties`:

```properties
spring.profiles.active=local
```

Then add it to `.gitignore` so it is never committed:

```
# .gitignore
application-local.properties
.env
```

> **Important:** add the `.gitignore` entry **before** you create the file. Once a file is tracked by git, adding it to `.gitignore` does not remove it from history.

---

## Summary — What to Commit vs What to Gitignore

| File | Commit? | Why |
|---|---|---|
| `application.properties` | ✅ Yes | No real secrets — `application-local.properties` overrides locally |
| `application-local.properties` | ❌ No | Contains real values |
| `.gitignore` | ✅ Yes | Protects the above file |

---

# (Optional / Advanced) GitHub Secrets

When you move beyond local development and want to run automated workflows (tests, deployments) on GitHub, your pipelines also need secrets — for example, to connect to a database or push a Docker image.

GitHub provides a built-in **Secrets** feature for this.

### Adding a Secret to a Repository

1. Go to your repository on GitHub
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Give it a name (e.g. `DB_PASSWORD`) and a value
5. Click **Add secret**

The secret is now stored encrypted in GitHub. You cannot read it back after saving — you can only update or delete it.

---

## GitHub Actions — Using Secrets in Workflows

**GitHub Actions** is GitHub's built-in CI/CD (Continuous Integration / Continuous Deployment) system — it runs automated workflows such as tests and deployments whenever code is pushed. You write workflow files in `.github/workflows/` and GitHub runs them automatically.

A workflow file is YAML. Secrets are injected as environment variables using `${{ secrets.SECRET_NAME }}`:

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - name: Check out code
        uses: actions/checkout@v4

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Run tests
        env:
          DB_USERNAME: ${{ secrets.DB_USERNAME }}
          DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
        run: ./mvnw test
```

### How it works

1. The workflow runs on GitHub's servers (not your laptop)
2. GitHub injects the secret values as environment variables for that step
3. The values are **masked** in the logs — if they accidentally appear in output, GitHub replaces them with `***`
4. The secrets are **never** visible in the workflow file itself

---

## 🔑 Types of GitHub Secrets

| Type | Scope | Where to set it |
|---|---|---|
| **Repository secret** | One specific repo | Settings → Secrets and variables → Actions |
| **Environment secret** | A deployment environment (e.g. `production`) | Settings → Environments |
| **Organization secret** | All repos in an organization | Organization Settings → Secrets |

For student projects, **repository secrets** are what you will use.

---

## Full Picture — Local vs GitHub Actions

```
Local Development
  └── application-local.properties (gitignored)  OR  IDE environment variables
         ↓
    Spring Boot reads ${DB_PASSWORD}

GitHub Actions (CI/CD)
  └── Secrets stored in GitHub Settings
         ↓
    Injected as env vars into the workflow
         ↓
    Spring Boot reads ${DB_PASSWORD}
```

The application code and `application.properties` stay exactly the same in both cases — only where the secret value comes from changes.
