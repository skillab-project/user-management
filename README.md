# User Management

A Spring Boot microservice responsible for user authentication, authorization, profile management, and API gateway functionality. Part of the [Skillab](https://skillab-project.eu) project ecosystem.

---

## Overview

This service handles:

- User registration, login, and JWT-based authentication
- Role-based access control (SIMPLE / PRIVILEGED)
- User profile management (skills, occupation, address, portfolio)
- Password reset via email
- System configuration per user
- Organization management
- API gateway / reverse proxy to some downstream microservices
- Asynchronous analysis orchestration via the external Skillab Tracker API

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 11 |
| Framework | Spring Boot 2.7.12 |
| Security | Spring Security + JWT (auth0 java-jwt) |
| Persistence | Spring Data JPA + PostgreSQL |
| Mail | Spring Mail (SMTP / Gmail) |
| HTTP Client | Unirest Java |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5, Mockito, H2 (in-memory) |
| Build | Maven (Maven) |
| Containerization | Docker + Docker Compose |

---

## Getting Started

### Prerequisites

- Java 11
- Maven 3.8+
- PostgreSQL (or Docker)
- A Gmail account (or other SMTP provider) for email functionality

### Local Setup

1. **Clone the repository**

```bash
git clone https://github.com/skillab-project/user-management
cd user-management
```

2. **Configure environment variables**

The application requires the following environment variables (or you can override them in `application.properties`):

| Variable | Description                                 |
|---|---------------------------------------------|
| `MAIL_USERNAME` | SMTP email username                         |
| `MAIL_PASSWORD` | SMTP email password (or gmail app password) |
| `TRACKER_USERNAME` | Tracker API username                        |
| `TRACKER_PASSWORD` | Tracker API password                        |
| `SPRING_DATASOURCE_URL` | The DB url                                  |
| `SPRING_DATASOURCE_USERNAME` | The DB username                             |
| `SPRING_DATASOURCE_PASSWORD` | The DB password                             |


3. **Run with Maven**

```bash
./mvnw spring-boot:run
```

The service starts on port **8080** by default.

4. **Run with Docker Compose**

```bash
export MAIL_USERNAME=your@email.com
export MAIL_PASSWORD=yourpassword
export TRACKER_USERNAME=trackeruser
export TRACKER_PASSWORD=trackerpass

docker compose up -d
```

This spins up the application on port **8081** alongside a PostgreSQL 14 database.

---

## Configuration

Key properties in `src/main/resources/application.properties`:

```properties
app.jwt.secret=secret                        # JWT signing secret — change in production!
app.installation=citizen                     # Installation type
app.admin.email=admin@skillab.eu             # Default admin email for the platform
app.admin.password=adminskillab             # Default admin password for the platform!
app.organization.name=Organization           # Default organization name

frontend.url=http://localhost:3000           # Frontend URL for password reset links
```

On startup, the service automatically creates the default organization and admin user if they don't already exist.

---

## API Reference

Swagger UI is available at:

```
http://localhost:8080/api-ui
```

OpenAPI JSON spec at:

```
http://localhost:8080/api
```

### Key Endpoints

#### Authentication

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/login` | Public | Login with `email` + `password` params; returns access & refresh tokens |
| GET | `/user/token/refresh` | Bearer (refresh token) | Obtain a new access token |

#### User

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/user` | Public | Register a new user |
| GET | `/user/{id}` | Bearer | Get user profile |
| PUT | `/user/{id}` | Bearer | Update country, street address, portfolio |
| PUT | `/user/{id}/skills` | Bearer | Add a skill |
| PUT | `/user/{id}/skills/skill` | Bearer | Update years of experience for a skill |
| DELETE | `/user/{id}/skills` | Bearer | Remove a skill |
| GET | `/user/{id}/skills` | Bearer | List user skills |
| PUT | `/user/{id}/occupation` | Bearer | Set target occupation |
| GET | `/user/{id}/configurations` | Bearer | Get system configuration |
| PUT | `/user/{id}/configurations` | Bearer | Update system configuration |
| POST | `/user/reset-password/request` | Public | Request a password reset email |
| PUT | `/user/reset-password` | Public | Complete password reset with token |

#### Admin (requires `PRIVILEGED` role)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/admin/users/all` | List all users |
| POST | `/admin/users/create` | Create a user with a specific installation and organization |
| PUT | `/admin/users/authorize` | Grant PRIVILEGED role to a user |
| DELETE | `/admin/users/delete` | Delete a user |
| PUT | `/admin/users/organization` | Change a user's organization |
| POST | `/admin/organization` | Create an organization |
| GET | `/admin/organization` | List all organizations |

#### Analysis

| Method | Endpoint | Description |
|---|---|---|
| GET | `/analysis/check` | Check if an analysis with the given filters already exists |
| POST | `/analysis/new` | Start a new asynchronous analysis |
| GET | `/analysis` | List all analyses |
| DELETE | `/analysis` | Delete an analysis |
| GET | `/analysis/{id}/check` | Get analysis status by ID |
| GET | `/analysis/{id}/descriptive` | Get descriptive analytics results |
| GET | `/analysis/{id}/descriptivelocation` | Get descriptive location results |
| GET | `/analysis/{id}/exploratory` | Get exploratory analytics results |
| GET | `/analysis/{id}/trend` | Get trend analytics results |

#### API Gateway

The service acts as a reverse proxy, forwarding authenticated requests to downstream services while injecting `X-User-Id`, `X-User-Email`, and `X-User-Organization` headers.

| Prefix | Target Service |
|---|---|
| `/hiring-management-backend/**` | Hiring Management |
| `/backend-policy/**` | Policy Backend |
| `/employee-management-backend/**` | Employee Management |
| `/ku-detection-backend/**` | KU Detection |
| `/policy-success-evaluator-backend/**` | Policy Success Evaluator |
| `/future-technology-trends-identifier-backend/**` | Future Technology Trends Identifier |

---

## Running Tests

```bash
./mvnw test
```

Tests use an H2 in-memory database and a mocked `JavaMailSender`, so no external dependencies are required.

---

## Project Structure

```
src/
├── main/java/gr/uom/user_management/
│   ├── config/          # Security, CORS, JWT filters, startup config
│   ├── controllers/     # REST controllers & DTOs
│   ├── models/          # JPA entities
│   ├── repositories/    # Spring Data repositories
│   ├── services/        # Business logic, mail, proxy, async analysis
│   └── utils/           # JWT utility
└── test/                # Tests
```

---

## License

This project is licensed under the [Eclipse Public License v2.0](LICENSE).
