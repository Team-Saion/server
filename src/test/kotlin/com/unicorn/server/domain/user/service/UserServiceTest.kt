package com.unicorn.server.domain.user.service

import com.unicorn.server.domain.user.User
import com.unicorn.server.domain.user.event.UserSignedUpEvent
import com.unicorn.server.domain.user.exception.DuplicateEmailException
import com.unicorn.server.domain.user.exception.UserNotFoundException
import com.unicorn.server.domain.user.port.dto.CreateUserRequest
import com.unicorn.server.domain.user.port.dto.UpdateUserRequest
import com.unicorn.server.domain.user.port.out.UserOutPort
import com.unicorn.server.domain.user.vo.Email
import com.unicorn.server.domain.user.vo.UserId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.context.ApplicationEventPublisher

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

	@Test
	@DisplayName("중복 이메일로 회원가입 시 DuplicateEmailException이 발생한다")
	fun register_withDuplicateEmail_throwsDuplicateEmailException() {
		userService.register(CreateUserRequest("exists@example.com", "testuser", "password123"))

		assertThatThrownBy {
			userService.register(CreateUserRequest("exists@example.com", "otheruser", "password123"))
		}.isInstanceOf(DuplicateEmailException::class.java)
	}

	@Test
	@DisplayName("존재하지 않는 ID로 조회 시 UserNotFoundException이 발생한다")
	fun getById_whenNotFound_throwsUserNotFoundException() {
		val userId = UserId.generate()

		assertThatThrownBy { userService.getById(userId) }
			.isInstanceOf(UserNotFoundException::class.java)
	}

	@Test
	@DisplayName("사용자 정보 수정이 성공한다")
	fun update_success() {
		val user = userService.register(CreateUserRequest("old@example.com", "olduser", "password123"))
		val request = UpdateUserRequest("newusername", null)

		val result = userService.update(user.id.toString(), request)

		assertThat(result.username).isEqualTo("newusername")
	}

	@Test
	@DisplayName("activate 호출 시 사용자 상태가 ACTIVE로 변경된다")
	fun activate_changesStatusToActive() {
		val user = userService.register(CreateUserRequest("test@example.com", "testuser", "password123"))

		userService.activate(user.id.toString())

		assertThat(userService.getById(user.id).isActive()).isTrue()
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

	private class RecordingEventPublisher : ApplicationEventPublisher {
		val events = mutableListOf<Any>()

		override fun publishEvent(event: Any) {
			events += event
		}
	}
}
