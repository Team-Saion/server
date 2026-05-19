# AI Guide

This file is the shared project instruction entry point for AI coding tools.

All AI agents working in this repository should read this file first, then read `AI_SKILLS.md` to select any project-local skills under `./skills` that match the task.

## Project Summary

This repository is a Kotlin + Spring Boot backend service.

Core stack:

- Kotlin/JVM
- Spring Boot
- Gradle Kotlin DSL
- Java 21 toolchain
- Spring Web
- Spring Data JPA
- Spring Validation
- Spring AOP/AspectJ
- H2 for local/test runtime
- PostgreSQL runtime driver
- JUnit 5 / AssertJ for tests

## Architecture Baseline

This project follows a Clean Architecture / Hexagonal Architecture / DDD-oriented style.

Current package intent:

```text
src/main/kotlin/com/unicorn/server/
├── domain/                 # business model, use-case ports, output ports, use-case services
├── infrastructure/adapter/  # web/event input adapters and persistence/redis/storage output adapters
└── common/                  # shared value objects, exceptions, annotations, and cross-cutting ports
```

Core rules:

- Domain entities and value objects must not depend on JPA, Spring Web, Redis, SDKs, HTTP clients, or persistence entities.
- Controllers are input adapters; they should call input ports and map responses.
- Persistence, Redis, object storage, and third-party integrations are output adapters.
- Use-case services coordinate domain objects and ports.
- Use-case services must not contain HTTP transport models, JPA entities, Redis templates, external SDK clients, or persistence models.
- Output adapters must map persistence/infrastructure models back to domain objects before crossing port boundaries.
- Prefer fake ports for use-case tests.

## Project-Specific Architecture Note

Domain entities/value objects remain framework-free, and infrastructure details belong behind ports and adapters.

## Required Skill Selection

Before implementing, inspect `AI_SKILLS.md` and load/read the relevant project-local skill files from `./skills`.

Minimum mapping:

- Architecture, module design, ports/adapters, DDD, dependency boundaries: `skills/architecture-patterns/SKILL.md`
- Unit tests, JUnit 5, AssertJ, fake ports, Spring Boot test context: `skills/junit/SKILL.md`

If more than one skill applies, read all relevant skill files.

## Coding Rules

- Preserve the existing Kotlin style and package conventions.
- Prefer small, explicit interfaces at architectural boundaries.
- Do not introduce `as any`-style unsafe shortcuts or equivalent type suppression patterns.
- Do not delete failing tests to make the build pass.
- Do not move business logic into controllers, JPA entities, Redis adapters, or configuration classes.
- Use comments only when the code's intent is not obvious from naming and structure.

## Verification Rules

After code changes, run the smallest relevant verification first, then broaden if needed.

Recommended commands:

```bash
./gradlew test
```

For targeted tests:

```bash
./gradlew test --tests "fully.qualified.TestClassName"
```

For documentation-only changes, at minimum inspect the diff and check for formatting/whitespace issues.
