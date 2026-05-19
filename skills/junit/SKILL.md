---
name: junit
description: Write and maintain Kotlin JUnit 5 tests for this Spring Boot server. Use when adding or refactoring unit tests, domain tests, use-case tests with fake ports, Spring Boot context tests, AssertJ assertions, Gradle test execution, or test naming conventions for this project.
---

# JUnit Testing

Use this skill to write fast, readable, and architecture-aware tests for this Kotlin + Spring Boot project.

This project uses:

- Gradle Kotlin DSL
- Kotlin/JVM
- Spring Boot test starter
- JUnit 5 / JUnit Jupiter
- `kotlin-test-junit5`
- AssertJ
- `tasks.withType<Test> { useJUnitPlatform() }`

## Test Layers

Prefer the smallest test that proves the behavior.

```text
Domain test             -> no Spring, no database, no fake ports unless needed
Use-case/service test   -> no Spring context, fake output ports, fake event publisher
Adapter/integration test -> Spring context or real infrastructure only when wiring matters
Context smoke test      -> @SpringBootTest + @ActiveProfiles("test")
```

## Project Test Style

Follow the existing test style:

```kotlin
@DisplayName("User 도메인 단위 테스트")
class UserTest {
    @Test
    @DisplayName("사용자 생성 시 초기 상태는 PENDING이다")
    fun create_initialStatusIsPending() {
        val user = User.create(Email("test@example.com"), "testuser", "hashed_pw")

        assertThat(user.status).isEqualTo(UserStatus.PENDING)
    }
}
```

Use:

- Korean `@DisplayName` for business-readable test descriptions when existing nearby tests do so.
- English method names in `action_condition_expectedResult` style.
- Arrange / Act / Assert spacing with blank lines between phases.
- AssertJ for fluent assertions: `assertThat`, `assertThatThrownBy`.

## Domain Unit Tests

Domain tests should instantiate domain objects directly and avoid Spring.

```kotlin
@Test
@DisplayName("잘못된 이메일 형식으로 Email 생성 시 예외가 발생한다")
fun email_withInvalidFormat_throwsException() {
    assertThatThrownBy { Email("not-an-email") }
        .isInstanceOf(IllegalArgumentException::class.java)
}
```

Rules:

- Do not use `@SpringBootTest` for pure domain behavior.
- Test state transitions and invariants on aggregate roots.
- Test value object validation at construction time.
- Avoid testing private implementation details.

## Use-Case Tests with Fake Ports

Use fake implementations for output ports. This keeps tests fast and proves the port boundary works.

```kotlin
@DisplayName("UserService 단위 테스트")
class UserServiceTest {
    private val userOutPort = FakeUserOutPort()
    private val eventPublisher = RecordingEventPublisher()
    private val userService = UserService(userOutPort, eventPublisher)

    @Test
    @DisplayName("정상적인 회원가입이 성공한다")
    fun register_success() {
        val request = CreateUserRequest("test@example.com", "testuser", "password123")

        val result = userService.register(request)

        assertThat(result.email.value).isEqualTo("test@example.com")
        assertThat(result.username).isEqualTo("testuser")
        assertThat(eventPublisher.events).anyMatch { it is UserSignedUpEvent }
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

Rules:

- Prefer fake ports over Mockito for domain/use-case tests when behavior is simple.
- Keep fakes private inside the test class unless reused across many tests.
- Use Mockito only when interaction verification is the actual behavior under test.
- Do not require PostgreSQL, Redis, Docker, or network for use-case tests.

## Spring Boot Context Tests

Use context tests only for wiring smoke checks.

```kotlin
@SpringBootTest
@ActiveProfiles("test")
class ServerApplicationTests {
    @Test
    fun contextLoads() {
    }
}
```

Rules:

- Keep context-load tests minimal.
- Use `application-test.yml` and `@ActiveProfiles("test")` for Spring tests.
- Do not put domain behavior assertions in context-load smoke tests.

## Exception Assertions

Use AssertJ exception assertions:

```kotlin
assertThatThrownBy { user.activate() }
    .isInstanceOf(IllegalStateException::class.java)
```

When the message is part of the contract, assert it explicitly:

```kotlin
assertThatThrownBy { Email("invalid") }
    .isInstanceOf(IllegalArgumentException::class.java)
    .hasMessageContaining("Invalid email")
```

## Running Tests

Run the smallest useful scope first, then the full suite when behavior is complete.

```bash
./gradlew test
```

For a single test class, use Gradle's test filter:

```bash
./gradlew test --tests "com.unicorn.server.domain.user.UserTest"
```

## Checklist

Before finishing test work, verify:

- The test proves behavior, not implementation details.
- Pure domain tests do not start Spring.
- Use-case tests use fake output ports where possible.
- Test names clearly describe action, condition, and expected result.
- Exceptions and domain events are asserted when they are part of behavior.
- The relevant Gradle test command passes.
