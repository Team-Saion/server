package com.unicorn.server.domain.member

import com.unicorn.server.domain.member.vo.MemberId
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("WithdrawalLog 도메인 단위 테스트")
class WithdrawalLogTest {

	@Test
	@DisplayName("reason이 blank이면 create에서 예외가 발생한다")
	fun create_whenReasonIsBlank_throwsException() {
		assertThatThrownBy {
			WithdrawalLog.create(MemberId.generate(), "test@example.com", " ", LocalDateTime.now())
		}.isInstanceOf(IllegalArgumentException::class.java)
	}

	@Test
	@DisplayName("reason이 500자를 초과하면 create에서 예외가 발생한다")
	fun create_whenReasonExceeds500Chars_throwsException() {
		assertThatThrownBy {
			WithdrawalLog.create(MemberId.generate(), "test@example.com", "a".repeat(501), LocalDateTime.now())
		}.isInstanceOf(IllegalArgumentException::class.java)
	}
}
