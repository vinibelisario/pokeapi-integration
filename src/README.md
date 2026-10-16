# pokeapi-integration

A Spring Boot REST API that lists and details Pokémon from PokeAPI, syncs them into a local PostgreSQL database, and lets authenticated users edit that local copy.

Built with Clean Architecture (domain, application, infrastructure, presentation) and TDD.

## Requirements

- Docker Desktop (for the container setup), or
- Java 21 and Maven 3.9+ and a PostgreSQL 16 instance (for the local setup)

## Run with Docker

```bash
docker compose up --build
```

The API runs at `http://localhost:8080`. On first start, a demo user `demo` (password `demo12345`) and three Pokémon (bulbasaur, charmander, pikachu) are created.

To change the secrets, set them before starting:

```bash
JWT_SECRET=your-own-long-secret DEMO_PASSWORD=your-password docker compose up --build
```

## Run locally

Start PostgreSQL, then:

```bash
mvn spring-boot:run
```

Defaults point to `localhost:5432` with user and password `pokeapi`. Override with `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`.

## Configuration

| Variable | Default | Purpose |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/pokeapi` | Database connection |
| `DB_USERNAME` / `DB_PASSWORD` | `pokeapi` / `pokeapi` | Database credentials |
| `JWT_SECRET` | development value | Signs tokens. **Set a real value outside development.** |
| `JWT_TTL` | `PT60M` | Token lifetime (ISO-8601 duration) |
| `DEMO_PASSWORD` | `demo12345` | Password of the seeded `demo` user |
| `SEED_ENABLED` | `true` | Set to `false` to skip demo data |
| `POKEAPI_BASE_URL` | `https://pokeapi.co/api/v2` | Upstream API |
| `POKEAPI_CACHE_TTL_MINUTES` | `10` | Cache lifetime for list pages |

## Endpoints

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | public | Create a user (201) |
| POST | `/api/auth/login` | public | Returns a JWT `token` |
| GET | `/api/pokemon?page=&size=` | public | Paginated list from PokeAPI |
| GET | `/api/pokemon/{idOrName}` | public | Details: image, stats, description, evolution chain |
| POST | `/api/local/pokemon/sync/{idOrName}` | Bearer | Save or refresh a Pokémon locally |
| GET | `/api/local/pokemon?page=&size=` | Bearer | Paginated list of local Pokémon |
| GET | `/api/local/pokemon/{id}` | Bearer | One local Pokémon |
| PUT | `/api/local/pokemon/{id}` | Bearer | Update `localizedName`, `region`, `tags` |
| DELETE | `/api/local/pokemon/{id}` | Bearer | Delete a local Pokémon (204) |

Errors use RFC 9457 `ProblemDetail` responses: 400 invalid data, 401 missing or invalid credentials, 404 not found, 409 conflict, 502 upstream failure.

## Authentication

1. Log in: `POST /api/auth/login` with `{"username":"demo","password":"demo12345"}`.
2. Send the returned token on protected requests: `Authorization: Bearer <token>`.

## Tests

```bash
mvn test
```

Tests cover the use cases with mocks, the PokeAPI and JPA adapters, the controllers with `MockMvc`, route security, and the Clean Architecture dependency rules (ArchUnit).

## Postman

Import `docs/postman/pokeapi-integration.postman_collection.json`. Run **Login** first; its script stores the token for the other requests.

## Project layout

```
src/main/java/com/pokeapi
├── domain            # pure Java models and exceptions
├── application       # use cases (services) and ports
├── infrastructure    # PokeAPI client, JPA, security, seeding, configuration
└── presentation      # controllers, DTOs, error handling
```