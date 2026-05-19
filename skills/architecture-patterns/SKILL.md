---
name: architecture-patterns
description: Implement proven backend architecture patterns including Clean Architecture, Hexagonal Architecture, and Domain-Driven Design for this Kotlin Spring Boot server. Use this skill when designing new bounded contexts, adding use cases, creating ports and adapters, refactoring infrastructure leaks, or debugging dependency cycles between domain, application/use-case, and infrastructure layers.
---

# Architecture Patterns

Master Clean Architecture, Hexagonal Architecture, and Domain-Driven Design in the style of this project: a Kotlin + Spring Boot backend where business concepts live in `domain`, external systems live behind `port` interfaces, and concrete Spring/JPA/Redis/web implementations live in `infrastructure/adapter`.

**Given:** a business capability or module to design.
**Produces:** package structure, dependency rules, port definitions, adapter boundaries, domain model examples, and test boundaries that keep the core business logic maintainable and testable.

The purpose of this skill is unchanged: keep business logic independent from delivery mechanisms, persistence models, and third-party infrastructure. The examples are written in Kotlin and mapped to the current project conventions.

## When to Use This Skill

Use this skill when:

- Designing a new backend module or bounded context.
- Adding a new use case to an existing domain such as `user`.
- Introducing a new external dependency: database, Redis, object storage, message broker, payment gateway, notification provider, or another service.
- Refactoring business logic out of controllers, JPA entities, Redis adapters, or framework configuration.
- Debugging dependency cycles where `domain` starts depending on `infrastructure`.
- Creating unit tests that should run without a real database, Redis, Docker, or network dependency.
- Implementing DDD tactical patterns: entities, value objects, aggregate roots, domain events, repositories/ports, and application services.

## Current Project Shape

This project currently follows a domain-first, port-and-adapter style:

```text
src/main/kotlin/com/unicorn/server/
├── common/
│   ├── annotation/
│   │   └── PersistenceAdapter.kt
│   ├── domain/
│   │   └── Event.kt
│   ├── exception/
│   ├── persistence/
│   ├── port/out/
│   │   ├── redis/
│   │   └── storage/
│   └── vo/
├── domain/
│   └── user/
│       ├── User.kt
│       ├── enums/
│       ├── event/
│       ├── exception/
│       ├── port/
│       │   ├── in/
│       │   ├── out/
│       │   └── dto/
│       ├── service/
│       └── vo/
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   ├── event/
    │   │   └── web/
    │   └── out/
    │       ├── persistence/
    │       ├── redis/
    │       └── storage/
    ├── aop/
    └── config/
```

Read these folders as architecture boundaries, not just naming conventions:

- `domain/<context>`: aggregate roots, value objects, domain events, domain exceptions, use-case ports, output ports, and current use-case services.
- `domain/<context>/port/in`: driving ports. These are use-case contracts called by controllers, schedulers, event consumers, or other input adapters.
- `domain/<context>/port/out`: driven ports. These are capabilities the core needs from the outside world, such as persistence or external APIs.
- `infrastructure/adapter/in`: input adapters. These translate HTTP requests, framework events, scheduled jobs, or messages into port calls.
- `infrastructure/adapter/out`: output adapters. These implement driven ports using JPA, Redis, object storage, external HTTP APIs, or SDKs.
- `common/port/out`: shared driven ports for cross-cutting infrastructure capabilities such as Redis or object storage.

## Core Concepts

### 1. Clean Architecture

Clean Architecture keeps framework and infrastructure details away from domain entities and value objects. Adapters depend inward through ports.

```text
infrastructure adapter -> port interface -> use-case service -> domain model
```

In this project, the rule means:

- Controllers may depend on input ports such as `RegisterUserInPort`.
- Persistence adapters may depend on output ports such as `UserOutPort`.
- Domain entities and value objects must not import Spring Web, JPA, Redis, HTTP clients, SDKs, or persistence entities.
- JPA entities and mappers stay in `infrastructure/adapter/out/persistence`.
- HTTP request handling stays in `infrastructure/adapter/in/web`.

### 2. Hexagonal Architecture: Ports and Adapters

For every external interaction, identify four pieces:

```text
Driver adapter  -> Controller, event listener, scheduler
Driver port     -> RegisterUserInPort, GetUserInPort, ManageUserInPort
Driven port     -> UserOutPort, RedisStore, ObjectStorage
Driven adapter  -> UserPersistenceAdapter, RedisStoreAdapter, S3StorageAdapter
```

Ports belong to the inner side of the boundary. Adapters belong to `infrastructure`.

### 3. Domain-Driven Design

Use DDD tactical patterns when business rules matter:

- **Entity / Aggregate Root**: has identity and lifecycle. Example: `User`.
- **Value Object**: immutable, validated at construction, equality by value. Example: `Email`, `UserId`, `ImageUrl`.
- **Domain Event**: fact in the past tense. Example: `UserSignedUpEvent`, `UserWithdrawnEvent`.
- **Port / Repository Abstraction**: project-owned interface for persistence or external capabilities. Example: `UserOutPort`.
- **Application Service / Use Case**: coordinates ports and domain objects. Example: `UserService`.
- **Adapter**: translates from framework or infrastructure details into the project-owned ports. Example: `UserController`, `UserPersistenceAdapter`, `RedisStoreAdapter`.

## Dependency Rules

Follow these rules when adding or changing code:

```text
Allowed:
infrastructure.adapter.in.web -> domain.<context>.port.in
infrastructure.adapter.out.persistence -> domain.<context>.port.out
infrastructure.adapter.out.persistence -> domain entity/value object
use-case service -> domain entity/value object
use-case service -> port.out
use-case service -> port.in implementation

disallowed:
domain entity -> JPA entity
domain entity -> Spring Web / Redis / SDK / HTTP client
use-case service -> JPA entity / RedisTemplate / external SDK client
controller -> JPA repository directly
controller -> persistence adapter directly
JPA entity -> controller DTO
output adapter -> input adapter
```

Use imports as an architecture test. If a file under `domain` imports a package under `infrastructure`, the boundary is broken.

## Recommended Package Structure for a New Context

For a new bounded context, prefer this shape:

```text
src/main/kotlin/com/unicorn/server/domain/order/
├── Order.kt
├── enums/
│   └── OrderStatus.kt
├── event/
│   └── OrderPlacedEvent.kt
├── exception/
│   ├── OrderErrorCode.kt
│   └── OrderNotFoundException.kt
├── port/
│   ├── dto/
│   │   ├── CreateOrderRequest.kt
│   │   └── OrderResponse.kt
│   ├── in/
│   │   ├── PlaceOrderInPort.kt
│   │   └── GetOrderInPort.kt
│   └── out/
│       └── OrderOutPort.kt
├── service/
│   └── OrderService.kt
└── vo/
    ├── OrderId.kt
    └── Money.kt

src/main/kotlin/com/unicorn/server/infrastructure/adapter/in/web/order/
└── OrderController.kt

src/main/kotlin/com/unicorn/server/infrastructure/adapter/out/persistence/order/
├── OrderJpaRepository.kt
├── OrderPersistenceAdapter.kt
├── entity/
│   └── OrderEntity.kt
└── mapper/
    └── OrderMapper.kt
```

Keep the shape consistent with the existing `user` context unless there is a clear reason to evolve the architecture.

## Kotlin Examples

### Entity / Aggregate Root

Keep the aggregate free of JPA and HTTP annotations. Model behavior as methods, not as external setters.

```kotlin
package com.unicorn.server.domain.user

import com.unicorn.server.domain.user.enums.UserStatus
import com.unicorn.server.domain.user.vo.Email
import com.unicorn.server.domain.user.vo.UserId
import java.time.LocalDateTime

class User private constructor(
    val id: UserId,
    email: Email,
    username: String,
    passwordHash: String,
    status: UserStatus,
    val createdAt: LocalDateTime,
    updatedAt: LocalDateTime,
) {
    var email: Email = email
        private set

    var username: String = username
        private set

    var passwordHash: String = passwordHash
        private set

    var status: UserStatus = status
        private set

    var updatedAt: LocalDateTime = updatedAt
        private set

    fun activate() {
        check(status != UserStatus.ACTIVE) { "User is already active" }
        check(status != UserStatus.DELETED) { "Deleted user cannot be activated" }
        status = UserStatus.ACTIVE
        updatedAt = LocalDateTime.now()
    }

    fun changeEmail(newEmail: Email) {
        email = newEmail
        updatedAt = LocalDateTime.now()
    }

    companion object {
        fun create(email: Email, username: String, passwordHash: String): User {
            require(username.isNotBlank()) { "Username cannot be blank" }
            require(username.length in 2..50) { "Username must be between 2 and 50 characters" }
            require(passwordHash.isNotBlank()) { "Password hash cannot be blank" }

            val now = LocalDateTime.now()
            return User(
                id = UserId.generate(),
                email = email,
                username = username,
                passwordHash = passwordHash,
                status = UserStatus.PENDING,
                createdAt = now,
                updatedAt = now,
            )
        }

        fun reconstitute(
            id: UserId,
            email: Email,
            username: String,
            passwordHash: String,
            status: UserStatus,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime,
        ): User = User(id, email, username, passwordHash, status, createdAt, updatedAt)
    }
}
```

### Value Object

Use Kotlin value classes for small validated concepts.

```kotlin
package com.unicorn.server.domain.user.vo

@JvmInline
value class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "Email cannot be blank" }
        require(EMAIL_PATTERN.matches(value.trim())) { "Invalid email format: $value" }
    }

    override fun toString(): String = value

    companion object {
        private val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
```

### Input Port

Input ports describe use cases in domain language. Controllers call these interfaces instead of concrete services.

```kotlin
package com.unicorn.server.domain.user.port.`in`

import com.unicorn.server.domain.user.User
import com.unicorn.server.domain.user.port.dto.CreateUserRequest

interface RegisterUserInPort {
    fun register(request: CreateUserRequest): User
}
```

### Output Port

Output ports describe capabilities the core needs from the outside world.

```kotlin
package com.unicorn.server.domain.user.port.out

import com.unicorn.server.domain.user.User
import com.unicorn.server.domain.user.vo.Email
import com.unicorn.server.domain.user.vo.UserId

interface UserOutPort {
    fun save(user: User): User
    fun findById(userId: UserId): User?
    fun findByEmail(email: Email): User?
    fun existsByEmail(email: Email): Boolean
}
```

### Use Case Service

A use-case service orchestrates domain objects and ports. It should not know HTTP request/response objects, JPA entities, Redis templates, or external SDKs.

```kotlin
package com.unicorn.server.domain.user.service

import com.unicorn.server.domain.user.User
import com.unicorn.server.domain.user.exception.DuplicateEmailException
import com.unicorn.server.domain.user.port.dto.CreateUserRequest
import com.unicorn.server.domain.user.port.`in`.RegisterUserInPort
import com.unicorn.server.domain.user.port.out.UserOutPort
import com.unicorn.server.domain.user.vo.Email

class UserService(
    private val userOutPort: UserOutPort,
) : RegisterUserInPort {
    override fun register(request: CreateUserRequest): User {
        val email = Email(request.email)

        if (userOutPort.existsByEmail(email)) {
            throw DuplicateEmailException(request.email)
        }

        val passwordHash = "{noop}${request.password}"
        val user = User.create(email, request.username, passwordHash)
        return userOutPort.save(user)
    }
}
```

### Input Adapter: REST Controller

A controller is an input adapter. It parses transport data, calls an input port, and maps the response.

```kotlin
package com.unicorn.server.infrastructure.adapter.`in`.web.user

import com.unicorn.server.domain.user.port.dto.CreateUserRequest
import com.unicorn.server.domain.user.port.dto.UserResponse
import com.unicorn.server.domain.user.port.`in`.RegisterUserInPort
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val registerUserInPort: RegisterUserInPort,
) {
    @PostMapping
    fun register(@RequestBody @Valid request: CreateUserRequest): ApiResponse<UserResponse> {
        val user = registerUserInPort.register(request)
        return ApiResponse.created(UserResponse.from(user))
    }
}
```

Controller rules:

- Do not call JPA repositories directly.
- Do not contain business rules such as duplicate checks, state transitions, or aggregate invariants.
- Do not return persistence entities.
- Keep validation of transport shape at the boundary; keep business invariants in domain objects.

### Output Adapter: JPA Persistence

A persistence adapter implements a domain-owned output port and maps between domain objects and JPA entities.

```kotlin
package com.unicorn.server.infrastructure.adapter.out.persistence.user

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.user.User
import com.unicorn.server.domain.user.port.out.UserOutPort
import com.unicorn.server.domain.user.vo.Email
import com.unicorn.server.domain.user.vo.UserId
import com.unicorn.server.infrastructure.adapter.out.persistence.user.mapper.UserMapper
import org.springframework.transaction.annotation.Transactional

@PersistenceAdapter
class UserPersistenceAdapter(
    private val userJpaRepository: UserJpaRepository,
    private val userMapper: UserMapper,
) : UserOutPort {
    @Transactional
    override fun save(user: User): User {
        val entity = userJpaRepository.findById(user.id.toString())
            .map { existing -> existing.apply { userMapper.updateEntity(this, user) } }
            .orElseGet { userMapper.toEntity(user) }

        return userMapper.toDomain(userJpaRepository.save(entity))
    }

    @Transactional(readOnly = true)
    override fun findById(userId: UserId): User? =
        userJpaRepository.findById(userId.toString())
            .map(userMapper::toDomain)
            .orElse(null)

    @Transactional(readOnly = true)
    override fun findByEmail(email: Email): User? =
        userJpaRepository.findByEmail(email.value)?.let(userMapper::toDomain)

    @Transactional(readOnly = true)
    override fun existsByEmail(email: Email): Boolean =
        userJpaRepository.existsByEmail(email.value)
}
```

Persistence rules:

- JPA annotations belong on persistence entities, not domain entities.
- Mapping belongs in infrastructure mappers or persistence entities, not controllers.
- Transactions belong at the adapter boundary or use-case boundary by deliberate decision; do not scatter transaction semantics through entities.

### Shared Output Adapter: Redis

For cross-cutting infrastructure such as Redis, define a shared port under `common/port/out` and implement it in `infrastructure/adapter/out`.

```kotlin
package com.unicorn.server.common.port.out.redis

import java.time.Duration

interface RedisStore {
    fun set(key: RedisKey, value: String)
    fun set(key: RedisKey, value: String, ttl: Duration)
    fun get(key: RedisKey): String?
    fun delete(key: RedisKey): Boolean
    fun exists(key: RedisKey): Boolean
    fun expire(key: RedisKey, ttl: Duration): Boolean
}
```

```kotlin
package com.unicorn.server.infrastructure.adapter.out.redis

import com.unicorn.server.common.port.out.redis.RedisKey
import com.unicorn.server.common.port.out.redis.RedisStore
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisStoreAdapter(
    private val redisTemplate: RedisTemplate<String, String>,
) : RedisStore {
    override fun set(key: RedisKey, value: String) {
        redisTemplate.opsForValue().set(key.value, value)
    }

    override fun set(key: RedisKey, value: String, ttl: Duration) {
        redisTemplate.opsForValue().set(key.value, value, ttl)
    }

    override fun get(key: RedisKey): String? =
        redisTemplate.opsForValue().get(key.value)

    override fun delete(key: RedisKey): Boolean =
        redisTemplate.delete(key.value) == true

    override fun exists(key: RedisKey): Boolean =
        redisTemplate.hasKey(key.value) == true

    override fun expire(key: RedisKey, ttl: Duration): Boolean =
        redisTemplate.expire(key.value, ttl) == true
}
```

## Testing with Fake Adapters

A correctly separated use case can be tested with fake ports and no real infrastructure.

```kotlin
package com.unicorn.server.domain.user.service

import com.unicorn.server.domain.user.User
import com.unicorn.server.domain.user.port.dto.CreateUserRequest
import com.unicorn.server.domain.user.port.out.UserOutPort
import com.unicorn.server.domain.user.vo.Email
import com.unicorn.server.domain.user.vo.UserId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("UserService unit test")
class UserServiceTest {
    private val userOutPort = FakeUserOutPort()
    private val userService = UserService(userOutPort)

    @Test
    fun register_success() {
        val result = userService.register(
            CreateUserRequest("test@example.com", "testuser", "password123"),
        )

        assertThat(result.email.value).isEqualTo("test@example.com")
        assertThat(result.username).isEqualTo("testuser")
    }

    private class FakeUserOutPort : UserOutPort {
        private val users = linkedMapOf<UserId, User>()

        override fun save(user: User): User {
            users[user.id] = user
            return user
        }

        override fun findById(userId: UserId): User? = users[userId]
        override fun findByEmail(email: Email): User? = users.values.firstOrNull { it.email == email }
        override fun existsByEmail(email: Email): Boolean = findByEmail(email) != null
    }
}
```

Testing rules:

- Domain tests should not start Spring.
- Use-case tests should use fake ports when the behavior under test is business orchestration.
- Adapter tests may start Spring or use Testcontainers when validating real JPA/Redis wiring.
- Do not delete or weaken tests to make architecture violations pass.

## Troubleshooting

### Use-case tests require a running database

Business logic has leaked into infrastructure. Move database access behind an output port and inject a fake implementation in unit tests.

### Controller contains duplicate checks, state transitions, or workflow logic

Move the logic into a use-case service. A controller should parse the request, call an input port, and map the response.

### Domain entity imports JPA annotations

Create a separate JPA entity in `infrastructure/adapter/out/persistence/<context>/entity` and map to/from the domain aggregate.

### Use-case service starts handling transport or persistence models

HTTP DTOs, JPA entities, Redis templates, SDK clients, and persistence models should stay in adapters. Move those details behind ports or mappers.

### Output adapter returns persistence entities

Map persistence entities back to domain objects before crossing the port boundary.

### Bounded contexts import each other's aggregate roots

Introduce a lightweight ID or snapshot value object, or add an anti-corruption port. Do not share aggregate internals across contexts.

## Review Checklist

Before accepting a new architecture change, check:

- Does the package location match the responsibility?
- Does `domain` avoid importing `infrastructure`?
- Are controllers depending on input ports rather than concrete services or repositories?
- Are external systems represented by output ports?
- Are JPA/Redis/SDK details confined to adapters?
- Are business invariants enforced by entities/value objects, not controllers?
- Can the use case be tested with fake ports?
- Is the new abstraction justified by a real boundary, not just ceremony?

## Advanced Patterns

For detailed DDD bounded context mapping, full multi-service project trees, Anti-Corruption Layer implementations, and Onion Architecture comparisons, see:

- [`references/advanced-patterns.md`](references/advanced-patterns.md)

## Related Skills

- `clean-ddd-hexagonal` — Apply these same rules as the project-wide architecture baseline.
- `architecture-patterns` — Use this project-local skill for Kotlin/Spring examples and package conventions.
