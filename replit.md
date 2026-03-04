# Shop Service

## Overview
Kotlin/Ktor backend service for an online shop with JWT authentication, PostgreSQL, Redis caching, and RabbitMQ messaging.

## Architecture
- **Language**: Kotlin (JVM 19)
- **Framework**: Ktor 2.3.7
- **Database**: PostgreSQL with Exposed ORM
- **Migrations**: Flyway
- **Auth**: JWT (java-jwt)
- **Caching**: Redis (Jedis)
- **Queue**: RabbitMQ
- **API Docs**: Swagger UI (ktor-swagger-ui)
- **Build**: Gradle 8.5 with fat JAR

## Project Structure
```
src/main/kotlin/com/shop/
├── Application.kt          - Entry point
├── config/                  - DatabaseConfig, JwtConfig
├── domain/model/            - DTOs and request models
├── domain/table/            - Exposed table definitions
├── repository/              - Data access layer
├── service/                 - Business logic
├── controller/              - HTTP route handlers
├── plugins/                 - Ktor plugins (Security, Serialization, etc.)
├── cache/                   - Redis caching
├── queue/                   - RabbitMQ producer
└── worker/                  - Background event worker
```

## Key Files
- `build.gradle.kts` - Dependencies and build config
- `src/main/resources/application.conf` - HOCON config
- `src/main/resources/db/migration/` - Flyway SQL migrations
- `src/main/resources/openapi/documentation.yaml` - OpenAPI spec
- `Dockerfile` / `docker-compose.yml` - Container config
- `.github/workflows/ci.yml` - CI/CD pipeline

## Running
The workflow builds a fat JAR and runs it on port 5000.
PostgreSQL is provided by Replit. Redis and RabbitMQ are optional (graceful fallback).

## Environment Variables
- DATABASE_URL, PGUSER, PGPASSWORD - Database
- JWT_SECRET - JWT signing key
- REDIS_HOST, REDIS_PORT - Redis
- RABBITMQ_HOST, RABBITMQ_PORT, RABBITMQ_ENABLED - RabbitMQ

## API Routes
- POST /auth/register, POST /auth/login
- GET /products, GET /products/{id}
- POST /products (admin), PUT /products/{id} (admin), DELETE /products/{id} (admin)
- POST /orders (auth), GET /orders (auth), DELETE /orders/{id} (auth)
- GET /stats/orders (admin)
- /swagger - API documentation
