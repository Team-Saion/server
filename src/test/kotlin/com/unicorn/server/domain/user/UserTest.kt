package com.unicorn.server.domain.user

import com.unicorn.server.domain.user.enums.UserStatus
import com.unicorn.server.domain.user.vo.Email
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("User 도메인 단위 테스트")
class UserTest {
	@Test
	@DisplayName("사용자 생성 시 초기 상태는 PENDING이다")
	fun create_initialStatusIsPending() {
		val user = User.create(Email("test@example.com"), "testuser", "hashed_pw")

		assertThat(user.status).isEqualTo(UserStatus.PENDING)
	}

	@Test
	@DisplayName("PENDING 상태의 사용자를 activate하면 ACTIVE가 된다")
	fun activate_fromPending_statusBecomesActive() {
		val user = User.create(Email("test@example.com"), "testuser", "hashed_pw")

		user.activate()

		assertThat(user.status).isEqualTo(UserStatus.ACTIVE)
	}

	@Test
	@DisplayName("이미 ACTIVE인 사용자를 activate하면 예외가 발생한다")
	fun activate_whenAlreadyActive_throwsException() {
		val user = User.create(Email("test@example.com"), "testuser", "hashed_pw")
		user.activate()

		assertThatThrownBy(user::activate)
			.isInstanceOf(IllegalStateException::class.java)
	}

	@Test
	@DisplayName("사용자 탈퇴 시 상태가 DELETED가 된다")
	fun delete_statusBecomesDeleted() {
		val user = User.create(Email("test@example.com"), "testuser", "hashed_pw")
		user.activate()

		user.delete()

		assertThat(user.status).isEqualTo(UserStatus.DELETED)
	}

	@Test
	@DisplayName("이미 삭제된 사용자를 다시 삭제하면 예외가 발생한다")
	fun delete_whenAlreadyDeleted_throwsException() {
		val user = User.create(Email("test@example.com"), "testuser", "hashed_pw")
		user.delete()

		assertThatThrownBy(user::delete)
			.isInstanceOf(IllegalStateException::class.java)
	}

	@Test
	@DisplayName("사용자명 변경이 반영된다")
	fun changeUsername_updatesUsername() {
		val user = User.create(Email("test@example.com"), "testuser", "hashed_pw")

		user.changeUsername("newusername")

		assertThat(user.username).isEqualTo("newusername")
	}

	@Test
	@DisplayName("잘못된 이메일 형식으로 Email 생성 시 예외가 발생한다")
	fun email_withInvalidFormat_throwsException() {
		assertThatThrownBy { Email("not-an-email") }
			.isInstanceOf(IllegalArgumentException::class.java)
	}
}
