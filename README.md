# mitsuru-home

A management system for a small **homestay with a coffee shop** in the same place. Guests can book
a room, and the admin manages rooms, bookings and the café. The café is just a normal coffee shop:
it takes drink orders and the guest gets an email about each one.

I'm building this project to learn new technologies, so it's a personal playground rather than a
production system. Some parts are deliberately split into separate services and languages just to
practice how they work together.

## What I'm learning

| Area | Technologies |
|---|---|
| Backend with Java | Java 25, Spring Boot 4, Spring Security + JWT, JPA / PostgreSQL, virtual threads |
| Backend with Go | Go services, MongoDB, GraphQL (gqlgen) |
| Messaging | RabbitMQ: durable queues, publisher confirms, a retry queue with TTL, a dead-letter queue (DLQ), reconnecting consumers |
| DevOps | Docker (multi-stage, layered images), Docker Compose, Jenkins CI |
| Tooling | Python scripts for generating fake test data |

## Features

**Coffee shop** (works now)
- [x] Admin login with JWT
- [x] Create and view drink orders
- [x] Order emails sent asynchronously through RabbitMQ, with retries and a DLQ

**Homestay** (planned)
- [ ] Room management for the admin (rooms, prices, max guests, amenities, status)
- [ ] Guest accounts
- [ ] Guests book a room; the admin confirms or cancels bookings
- [ ] Booking confirmation emails through the same queue

**Café** (planned)
- [ ] Drink menu management
- [ ] Guests staying at the homestay can order from the café

## How an order flows

```
           POST /orders                 order.exchange ──► order.queue
client ──► java-admin-service ──────────────────────────────────┐
              │  (Spring Boot, JWT)                             ▼
              ▼                                         go-email-service
          PostgreSQL                         failure ──► order.queue.retry (10s TTL) ──► back to order.queue
                                             3 retries / bad JSON ──► order.queue.dlq
```

## Services

| Directory | Stack | What it does |
|---|---|---|
| [`admin/`](admin) | Java 25, Spring Boot 4, Gradle | REST API with JWT login. Stores orders in PostgreSQL and publishes each new order to RabbitMQ (publisher confirms, so a failed publish rolls the order back). Uses virtual threads. |
| [`go-email-service/`](go-email-service) | Go 1.25, amqp091-go | Consumes `order.queue` and sends the order email. Failed messages go to `order.queue.retry` (10 s delay, up to 3 retries), then to `order.queue.dlq`. Reconnects automatically if RabbitMQ goes away. |
| [`seed-data/`](seed-data) | Python, pymongo, mimesis | Generates 10,000 fake accounts into the `mitsuru-home` MongoDB database. |

## Quick start (Docker Compose)

Compose runs PostgreSQL, RabbitMQ, `java-admin-service` and `go-email-service`.

```sh
cp .env.example .env      # then fill in DB_PASSWORD, JWT_SECRET, ADMIN_PASSWORD
openssl rand -base64 48   # a good value for JWT_SECRET (min. 32 chars)
docker compose up --build
```

| Endpoint | URL |
|---|---|
| Admin API | http://localhost:8080 |
| RabbitMQ management UI | http://localhost:15672 (user/pass from `.env`, default `guest`/`guest`) |
| PostgreSQL | `localhost:5432`, database `mitsuru_home` |

### Configuration

All secrets come from the root `.env` (see [`.env.example`](.env.example)); it is git-ignored, never commit it.

| Variable | Used by | Default |
|---|---|---|
| `DB_USERNAME` / `DB_PASSWORD` | Postgres, admin | `mitsuru` / *(required)* |
| `RABBITMQ_USERNAME` / `RABBITMQ_PASSWORD` | RabbitMQ, admin, email service | `guest` / `guest` |
| `JWT_SECRET` | admin | *(required, ≥ 32 chars)* |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` / `ADMIN_EMAIL` | admin login account | `admin` / *(required)* / `admin@example.com` |

## Admin API

All endpoints except `POST /login` need an `Authorization: Bearer <token>` header.

```sh
# log in
TOKEN=$(curl -s localhost:8080/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"<ADMIN_PASSWORD>"}' | jq -r .accessToken)

# create an order (published to RabbitMQ and picked up by go-email-service)
curl -s localhost:8080/orders \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"drinkId":1,"price":4.50,"note":"less sugar"}'

# list orders / get one
curl -s localhost:8080/orders   -H "Authorization: Bearer $TOKEN"
curl -s localhost:8080/orders/1 -H "Authorization: Bearer $TOKEN"
```

| Method | Path | Body |
|---|---|---|
| `POST` | `/login` | `{ "username", "password" }` → `{ "accessToken", "tokenType", "expiresInSeconds" }` |
| `GET` | `/orders` | – |
| `GET` | `/orders/{orderId}` | – |
| `POST` | `/orders` | `{ "drinkId", "price", "note"? }` → `201 Created` |

## Running services locally

Start the infrastructure with `docker compose up postgresql rabbitmq`, then:

**admin**

```sh
cd admin
./gradlew bootRun   # reads ../.env automatically
./gradlew test
```

**go-email-service** — needs `MQ_SERVER` and `ORDER_QUEUE` (from the environment or a `.env` in its directory):

```sh
cd go-email-service
MQ_SERVER=amqp://guest:guest@localhost:5672/ ORDER_QUEUE=order.queue go run .
go test ./...
```

**seed-data** — expects MongoDB at `localhost:27017`:

```sh
cd seed-data
pip install pymongo pandas mimesis
python seed.py
```

## CI

`admin/Jenkinsfile` and `go-email-service/model/Jenkinsfile` define the Jenkins pipelines for the two
message-queue services.
