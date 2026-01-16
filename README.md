# mini-doodle (Spring Boot) — Mini Meeting Scheduling Platform

A high-performance “mini Doodle” backend service for managing personal availability and scheduling meetings.

This service allows:
- Users to create and manage time slots (`AVAILABLE` / `BUSY`)
- Converting an `AVAILABLE` slot into a meeting (booking)
- Automatically marking participants as `BUSY` for the meeting time
- Querying free/busy slots per user and aggregated availability for a timeframe

---

## Features

### Time slot management
- Create time slots (with configurable duration)
- Update existing slots (start time, duration, status)
- Delete slots (only if not linked to a meeting)

### Meeting scheduling
- Convert organizer’s `AVAILABLE` slot into a meeting
- Attach meeting metadata: title, description, participants
- Create `BUSY` slots for participants for the meeting window

### Availability queries
- Fetch a user’s slots in a time range
- Compute per-user free/busy
- Compute common free intervals across a set of users in a time range

---

## Tech Stack

- Java 17+ (works with Java 21 too)
- Spring Boot 3.5.x
- Spring Web + Validation
- Spring Data JPA (Hibernate)
- PostgreSQL 16
- Flyway migrations (**Postgres support module required**)
- OpenAPI (Swagger UI)
- Actuator + Prometheus metrics
- Testcontainers for integration tests

---

## Quick Start (Docker Compose)

### Prerequisites (Ubuntu)
```bash
sudo apt update
sudo apt install -y git docker.io docker-compose-plugin
sudo systemctl enable --now docker
```

### Run the service
From the project root (where `docker-compose.yml` is):
```bash
docker compose up --build
```

If you didn’t add your user to the docker group:
```bash
sudo docker compose up --build
```

### URLs
- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Health: `http://localhost:8080/actuator/health`
- Prometheus metrics: `http://localhost:8080/actuator/prometheus`
- Prometheus UI: `http://localhost:9090`

### Stop
```bash
docker compose down
```

Remove DB volume data too:
```bash
docker compose down -v
```

---

## Testing

### Fast compile check
```bash
mvn -DskipTests compile
```

### Integration tests (Failsafe + Testcontainers)
If you use `*IT.java` integration tests, run:
```bash
mvn verify
```

Reports:
- Unit tests: `target/surefire-reports/`
- Integration tests: `target/failsafe-reports/`

---

## API Overview

Base path: `/api/v1`

### Endpoints
- `POST /users` — create user
- `GET /users/{id}` — get user
- `POST /users/{userId}/slots` — create slot
- `GET /users/{userId}/slots?from=...&to=...&status=...` — list slots in range
- `PATCH /slots/{slotId}` — update slot
- `DELETE /slots/{slotId}` — delete slot (fails if linked to meeting)
- `POST /meetings` — schedule meeting (book a slot)
- `GET /meetings/{id}` — get meeting
- `GET /availability?userIds=...&from=...&to=...` — free/busy per user + common free

> All timestamps are ISO-8601 UTC (e.g. `2026-01-15T10:00:00Z`).

---

## Example Usage (curl)

### Create users
```bash
curl -s -X POST http://localhost:8080/api/v1/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"alice@test.com","name":"Alice"}'
```

```bash
curl -s -X POST http://localhost:8080/api/v1/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"bob@test.com","name":"Bob"}'
```

### Create an AVAILABLE slot for Alice
```bash
curl -s -X POST http://localhost:8080/api/v1/users/<ALICE_ID>/slots \
  -H 'Content-Type: application/json' \
  -d '{"start":"2026-01-15T10:00:00Z","durationMinutes":60,"status":"AVAILABLE"}'
```

### Book that slot into a meeting (Alice organizer + Bob participant)
```bash
curl -s -X POST http://localhost:8080/api/v1/meetings \
  -H 'Content-Type: application/json' \
  -d '{
    "organizerId":"<ALICE_ID>",
    "slotId":"<SLOT_ID>",
    "title":"Design Review",
    "description":"API + DB constraints review",
    "participantIds":["<BOB_ID>"]
  }'
```

### List Alice slots in a timeframe
```bash
curl -s "http://localhost:8080/api/v1/users/<ALICE_ID>/slots?from=2026-01-15T00:00:00Z&to=2026-01-16T00:00:00Z"
```

### Availability aggregation (free/busy + common free)
```bash
curl -s "http://localhost:8080/api/v1/availability?userIds=<ALICE_ID>&userIds=<BOB_ID>&from=2026-01-15T00:00:00Z&to=2026-01-16T00:00:00Z"
```

---

## Observability

Actuator endpoints:
- `GET /actuator/health`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`

Prometheus is available (via docker-compose) at:
- `http://localhost:9090`

---
