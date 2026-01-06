# Repository Guidelines

## Project Structure & Module Organization
- `src/main/java/br/com/redemaisfarma` follows hexagonal packages: `domain` (entities/enums), `application` (controllers, ports, services, mappers, validation), `adapters` inbound/outbound (web filters/security/openapi, JPA, Firebird legacy, cache, external clients), plus `config` and the Spring Boot entrypoint.
- `src/main/resources` holds Spring profile configs (`application*.yml`), logging (`logback-spring.xml`), and static/templates.
- `src/test/java` mirrors main packages; `unit/` captures small-scope tests. `src/test/resources` includes `application-test.yml` and Mockito extensions.
- Infrastructure helpers live in `infra`, mock payloads in `mocks`, and docker-compose files stay at the repo root alongside `pom.xml` and `mvnw` scripts.

## Build, Test, and Development Commands
- `./mvnw clean verify` runs compile, unit/integration tests, Checkstyle, SpotBugs, and Jacoco coverage.
- `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` boots the API against local MySQL; use `docker-compose -f docker-compose.dev.yml up -d mysql` to start the DB container.
- `./mvnw test -Dspring.profiles.active=test` exercises the suite with Testcontainers (MySQL) and profile-specific settings.
- `./mvnw spotbugs:check` and `./mvnw checkstyle:check` are available for quick static analysis during iteration.
- Add `-Dspring.profiles.active=firebird` when you need legacy Firebird access or production-tuned settings.

## Coding Style & Naming Conventions
- Java 21 with Spring Boot 3.3, MapStruct, and Lombok. Use 4-space indentation and keep lines under ~120 characters.
- Package by layer as above; classes use PascalCase, fields camelCase. Controllers end with `Controller`, DTOs with `RequestDTO`/`ResponseDTO`, and mappers with `Mapper`.
- Prefer constructor injection; avoid field injection. Keep secrets/config in env files or profile YAML—do not commit sensitive values.

## Testing Guidelines
- JUnit 5, Mockito, and Testcontainers are available. Place unit tests near matching packages and name them `*Test`.
- Keep integration tests isolated (containers start automatically with the `test` profile); mock external HTTP/legacy calls where possible.
- Ensure Jacoco passes on `clean verify`; add assertions for both success and failure paths.

## Commit & Pull Request Guidelines
- Write concise, present-tense commits and keep scope focused. Examples: `feat: add product sync endpoint`, `fix: handle missing firebird file`.
- Before opening a PR: describe motivation and approach, list tests executed (`./mvnw clean verify`), link issues/tasks, and note config changes (new env vars, ports, profiles). Update OpenAPI/README when endpoints change.
