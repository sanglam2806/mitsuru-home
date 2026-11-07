# mitsuru-home

A small polyglot backend playground: a Java admin service that takes orders, a Go worker that
emails customers about them, and a MongoDB seeding script.

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
