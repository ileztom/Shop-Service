# Shop Service API

Backend-сервис на Kotlin/Ktor для интернет-магазина с JWT авторизацией, PostgreSQL, Redis кэшированием и RabbitMQ очередями.

## Архитектура

```
src/main/kotlin/com/shop/
├── Application.kt          # Точка входа
├── config/                 # Конфигурация (DB, JWT)
│   ├── DatabaseConfig.kt
│   └── JwtConfig.kt
├── domain/                 # Доменные модели
│   ├── model/Models.kt     # DTO и Request/Response модели
│   └── table/Tables.kt     # Exposed таблицы
├── repository/             # Слой данных
│   ├── UserRepository.kt
│   ├── ProductRepository.kt
│   ├── OrderRepository.kt
│   ├── AuditLogRepository.kt
│   └── StatsRepository.kt
├── service/                # Бизнес-логика
│   ├── AuthService.kt
│   ├── ProductService.kt
│   ├── OrderService.kt
│   └── StatsService.kt
├── controller/             # HTTP маршруты
│   ├── AuthController.kt
│   ├── ProductController.kt
│   ├── OrderController.kt
│   └── StatsController.kt
├── plugins/                # Ktor плагины
│   ├── Security.kt
│   ├── Serialization.kt
│   ├── StatusPages.kt
│   ├── Routing.kt
│   └── Swagger.kt
├── cache/
│   └── RedisCache.kt       # Redis кэширование
├── queue/
│   └── RabbitMQProducer.kt  # RabbitMQ продюсер
└── worker/
    └── OrderEventWorker.kt  # Фоновый обработчик событий
```

## Технологии

- **Kotlin** + **Ktor** — фреймворк
- **PostgreSQL** — база данных
- **Exposed ORM** — работа с БД
- **Flyway** — миграции
- **JWT (java-jwt)** — авторизация
- **Redis (Jedis)** — кэширование
- **RabbitMQ** — очередь сообщений
- **Swagger UI** — документация API
- **Docker / Docker Compose** — контейнеризация
- **GitHub Actions** — CI/CD
- **TestContainers** — интеграционные тесты

## API Endpoints

### Аутентификация
| Метод | Путь | Описание |
|-------|------|----------|
| POST | `/auth/register` | Регистрация пользователя |
| POST | `/auth/login` | Авторизация |

### Товары
| Метод | Путь | Доступ | Описание |
|-------|------|--------|----------|
| GET | `/products` | Публичный | Список всех товаров |
| GET | `/products/{id}` | Публичный | Товар по ID |
| POST | `/products` | Admin | Создать товар |
| PUT | `/products/{id}` | Admin | Обновить товар |
| DELETE | `/products/{id}` | Admin | Удалить товар |

### Заказы
| Метод | Путь | Доступ | Описание |
|-------|------|--------|----------|
| POST | `/orders` | User | Создать заказ |
| GET | `/orders` | User | Мои заказы |
| DELETE | `/orders/{id}` | User | Отменить заказ |

### Статистика
| Метод | Путь | Доступ | Описание |
|-------|------|--------|----------|
| GET | `/stats/orders` | Admin | Статистика заказов |

### Документация
| Путь | Описание |
|------|----------|
| `/swagger` | Swagger UI |

## Запуск

### Локально (Gradle)

```bash
# Сборка
./gradlew buildFatJar

# Запуск (требуется PostgreSQL)
export DATABASE_URL=jdbc:postgresql://localhost:5432/shop
export PGUSER=postgres
export PGPASSWORD=postgres
java -jar build/libs/shop-service.jar
```

### Docker Compose

```bash
docker-compose up -d
```

Поднимаются: приложение (порт 5000), PostgreSQL (5432), Redis (6379), RabbitMQ (5672, 15672).

## Переменные окружения

| Переменная | По умолчанию | Описание |
|-----------|-------------|----------|
| `PORT` | `5000` | Порт приложения |
| `DATABASE_URL` | — | URL базы данных |
| `PGUSER` | `postgres` | Пользователь БД |
| `PGPASSWORD` | — | Пароль БД |
| `JWT_SECRET` | встроенный | Секрет JWT |
| `REDIS_HOST` | `localhost` | Хост Redis |
| `REDIS_PORT` | `6379` | Порт Redis |
| `RABBITMQ_HOST` | `localhost` | Хост RabbitMQ |
| `RABBITMQ_PORT` | `5672` | Порт RabbitMQ |
| `RABBITMQ_ENABLED` | `false` | Включить RabbitMQ |

## База данных

5 таблиц: `users`, `products`, `orders`, `order_items`, `audit_logs`

Миграции через Flyway (`src/main/resources/db/migration/`).

## Тестирование

```bash
# Unit тесты
./gradlew test --tests "com.shop.unit.*"

# Integration тесты (TestContainers)
./gradlew test --tests "com.shop.integration.*"

# E2E тесты
./gradlew test --tests "com.shop.e2e.*"

# Все тесты
./gradlew test
```

## Бизнес-логика

При создании заказа:
1. Проверяется наличие товара на складе
2. Уменьшается `stock` товара
3. Создаётся запись в `audit_logs`
4. Отправляется событие в RabbitMQ
5. Данные заказа кэшируются в Redis

При отмене заказа:
1. Восстанавливается `stock` товаров
2. Статус заказа меняется на `CANCELLED`
3. Кэш очищается

## Кэширование (Redis)

- Список товаров кэшируется
- Отдельные товары кэшируются
- TTL по умолчанию: 300 сек
- Кэш очищается при создании/обновлении/удалении товаров
