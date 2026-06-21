# User Service + Auth Service 👤💳🔐

**User Service** is a core microservice of the online marketplace, responsible for securely managing user profiles and their associated payment cards. It operates tightly alongside a containerized **Auth Service (Keycloak)**, which handles identity management, centralized user authentication, and token issuance. Both services feature completely isolated database layers to ensure domain segregation.

---

## 🛠 Tech Stack

* **Core Framework:** Java 17, Spring Boot 3.x, Spring Security (OAuth2 Resource Server).
* **Database & Versioning:** PostgreSQL 15, Liquibase, Spring Data JPA (with JPA Auditing enabled).
* **Caching:** Redis 7 (for user profile data caching).
* **Identity & Access Management:** Keycloak 24.0 (JWT-based token validation).
* **Object Mapping & Utility:** MapStruct, Lombok.
* **Observability & Monitoring:** Micrometer (with custom Trace ID and Span ID logging integration), Spring Boot Actuator.
* **DevOps & Deployment:** Docker, Docker Compose, Kubernetes (K8s manifests customized for Minikube local testing), CI Pipeline.

---

## 🔐 Auth Service Infrastructure (Keycloak)

Authentication and Access Control are handled via an out-of-the-box IAM solution integrated directly into the local environment orchestration:
* **Standalone IAM Engine:** Powered by Keycloak 24.0 running in its own container (`keycloak`) to manage user credentials and OAuth2 flows.
* **Dedicated Authentication DB:** Keycloak utilizes an entirely separate PostgreSQL database instance (`keycloak-postgres`), ensuring that identity data is decoupled from the transactional application data (`user-db`).
* **Realm Protection:** Validates and handles security scopes through a customized target realm (`market-realm`), enabling token verification routines across the marketplace.

---

## 🚀 Key Features

### 🧑 User Profile & Card Management
* **Public Registration:** Endpoints for creating a local user profile linked to an external `keycloakId`.
* **Self-Service Profiling:** Allows users to view their profile details (`/users/me`), update personal data, and execute a soft delete on their account.
* **Secure Payment Cards:**
    * Allows users to link payment cards with strict input validation (exactly 16 digits, mandatory future expiration date).
    * Enforces business rules such as a configurable maximum card limit per person, duplicate card checks, and strict immutability on core card properties.
    * Formats outgoing card details securely by masking numbers (`**** **** **** 1234`).

### 👑 Administrative Management (Requires `ADMIN` Role)
* **Advanced User Search:** Administrators can perform comprehensive lookups using query filters (partial name/surname match, exact email, active status, birth date ranges, and profile creation timeframes) along with pagination and multi-field sorting.
* **Account & Card Control:** Administrative capabilities to manually activate or deactivate user accounts and payment cards to enforce platform policies.
* **Forced Soft Delete:** Admins can deactivate or soft-delete any user account on demand.

### 🪵 Observability & Production Readiness
* **Distributed Tracing:** Micrometer integration ensures that every API call logs explicit Trace and Span IDs, facilitating rapid debugging across distributed boundaries.
* **Kubernetes Probes:** Ready-to-use Actuator endpoints configured for Kubernetes health and readiness evaluation.

---

## 📐 API Overview

All endpoints (except for the public registration route) are fully secured and require a valid JWT token passed via the `Authorization: Bearer <token>` header.

### Client Domain (`User` Role)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/users` | Registers a new user account (**Public**) |
| `GET` | `/users/me` | Fetches the profile of the currently authenticated user |
| `PUT` | `/users/me` | Updates personal profile details |
| `DELETE`| `/users/me` | Soft-deletes the active user's own account |
| `GET` | `/users/my-cards` | Retrieves all payment cards linked to the active account |
| `POST` | `/users/my-cards` | Links a new payment card to the account (validates limits) |
| `GET` | `/users/my-cards/{cardId}` | Fetches details for a specific card owned by the user |
| `DELETE`| `/users/my-cards/{cardId}` | Unlinks and deletes a specific payment card |
| `PUT` | `/users/my-cards/{cardId}/activate` | Manually activates a payment card |
| `PUT` | `/users/my-cards/{cardId}/deactivate`| Manually deactivates a payment card |

### Administrative Domain (`ADMIN` Role)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/admin/users` | Filters, paginates, and sorts through all system users |
| `GET` | `/admin/users/{id}` | Fetches any user's profile by their unique internal ID |
| `DELETE`| `/admin/users/{id}` | Performs an administrative soft delete on a user account |
| `PUT` | `/admin/users/{id}/activate` | Admin-level activation of a user account |
| `PUT` | `/admin/users/{id}/deactivate` | Admin-level deactivation of a user account |
| `GET` | `/admin/users/{userId}/cards` | Inspects all payment cards belonging to a specific user |
| `PUT` | `/admin/users/{userId}/cards/{cardId}/activate` | Forces the activation of a specific user's card |
| `PUT` | `/admin/users/{userId}/cards/{cardId}/deactivate` | Forces the deactivation of a specific user's card |

> **Error Handling:** Standardized machine-readable error bodies (`ErrorResponse`) are thrown consistently across the API layer for predictable client-side handling (e.g., `CARDS_COUNT_LIMIT_OVERFLOW`, `EMAIL_DUPLICATE_CONFLICT`).

---

## ⚙️ Environment Configuration

Ensure that you populate your local `.env` file before initiating a build. The main variables used by the system components are outlined below:

```env
# Database Settings
USER_DB_NAME=user_db
USER_DB_USER=postgres
USER_DB_PASSWORD=secret_password
USER_DB_PORT_EXTERNAL=5432

# Redis Settings
REDIS_PASSWORD=redis_secret_pass
REDIS_PORT_EXTERNAL=6379

# Keycloak IAM Settings
KC_ADMIN_USER=admin
KC_ADMIN_PASSWORD=admin
KC_DB_NAME=keycloak_db
KC_DB_USER=keycloak
KC_DB_PASSWORD=keycloak_secret
KC_PORT_EXTERNAL=8080
KC_REALM_NAME=market-realm

# User Service Settings
USER_SERVICE_PROFILE=docker
USER_SERVICE_PORT_INTERNAL=8080
USER_SERVICE_PORT_EXTERNAL=8081
```

---

## 🏃 Getting Started

### Prerequisites
Before spinning up the containers, establish the external Docker network required for cross-service communication:
```bash
docker network create local_market_net
```

### Option 1: Running with Docker Compose
To build the application package from source code and spin up the complete local environment infrastructure (PostgreSQL instances, Redis, Keycloak, and the service container):

```bash
docker compose up -d --build
```
* **User Service API:** Exposed and accessible locally via the external port (defaults to `8081`).
* **Keycloak Console:** Available at `http://localhost:8080`.
* **Health Status:** Validate application readiness using the Actuator check from your host machine: `http://localhost:8081/actuator/health`.

### Option 2: Local Deployment inside Kubernetes (Minikube)
Deployment manifests are structured to map directly to local builds. Execute the following steps inside your local cluster:

1. Point your shell's Docker daemon environment directly to Minikube:
   ```bash
   eval $(minikube docker-env)
   ```
2. Build the target service image within the cluster environment:
   ```bash
   docker build -t user-service:latest .
   ```
3. Ensure your Kubernetes secret resources are created in the cluster before deploying. The manifests expect the following variables to be defined within your secrets:

    * **`auth-service-secret`** (Required for Keycloak server):
        * `KEYCLOAK_ADMIN` — Initial admin username for the Keycloak console.
        * `KEYCLOAK_ADMIN_PASSWORD` — Admin password for the Keycloak console.
        * `KC_DB_USERNAME` — Database username used by the Keycloak server instance.
        * `KC_DB_PASSWORD` — Database password used by the Keycloak server instance.

    * **`auth-db-secret`** (Required for Keycloak database infrastructure):
        * `POSTGRES_DB` — Name of the Keycloak database.
        * `POSTGRES_USER` — Username for the Keycloak database.
        * `POSTGRES_PASSWORD` — Password for the Keycloak database.

    * **`user-service-secret`** (Required for the core application backend):
        * `DB_USERNAME` — Database username for the core service.
        * `DB_PASSWORD` — Security password for the main Postgres database.
        * `REDIS_DATABASE` — Redis database index (e.g., `0`).
        * `REDIS_PASSWORD` — Security password for the Redis cache instance.

    * **`user-db-secret`** (Required for the main service database infrastructure):
        * `POSTGRES_DB` — Name of the application database.
        * `POSTGRES_USER` — Name of the application database user.
        * `POSTGRES_PASSWORD` — Security password for the database user.

4. **Configure Local DNS (`hosts` file):**
   Since Keycloak is configured with a strict hostname (`auth.market.local`), you must map your Minikube IP to this domain on your host machine to access the administration console.

    * **Linux / macOS:** Run the following command in your terminal:
      ```bash
      echo "$(minikube ip) auth.market.local" | sudo tee -a /etc/hosts
      ```
    * **Windows (PowerShell as Admin):** Run the following command:
      ```powershell
      Add-Content -Path C:\Windows\System32\drivers\etc\hosts -Value "$((minikube ip)) auth.market.local"
      ```

5. Apply the predefined resource configurations inside the folder:
   ```bash
   kubectl apply -f . --recursive
   ```

*The Kubernetes configuration features active `livenessProbe` and `readinessProbe` checking routines on port 8080 alongside defined resource memory limits (`512Mi` for the service instance and `1Gi` for Keycloak).*