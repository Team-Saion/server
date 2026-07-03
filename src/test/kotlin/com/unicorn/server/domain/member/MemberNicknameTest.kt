package com.unicorn.server.domain.member

import com.unicorn.server.TestIdFactory
import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.exception.MemberErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Member 닉네임 검증 단위 테스트")
class MemberNicknameTest {

	@Test
	@DisplayName("한글, 영문/숫자, 10자 한글 닉네임은 유효하다")
	fun nickname_withValidValues_succeeds() {
		val korean = member("korean@example.com", "홍길동", "홍길동")
		val englishNumber = member("english@example.com", "abc", "abc123")
		val tenKorean = member("ten@example.com", "가나다", "가나다라마바사아자차")

		assertThat(korean.nickname).isEqualTo("홍길동")
		assertThat(englishNumber.nickname).isEqualTo("abc123")
		assertThat(tenKorean.nickname).isEqualTo("가나다라마바사아자차")
	}

	@Test
	@DisplayName("잘못된 닉네임은 INVALID_NICKNAME 예외가 발생한다")
	fun nickname_withInvalidValues_throwsInvalidNickname() {
		val invalidNicknames = listOf(
			"a",
			"a".repeat(11),
			"abc!",
			"abc def",
			"😀",
			" abc",
			"abc ",
		)

		invalidNicknames.forEachIndexed { index, nickname ->
			assertThatThrownBy { member("invalid$index@example.com", "홍길동", nickname) }
				.isInstanceOf(BusinessException::class.java)
				.extracting("errorCode")
				.isEqualTo(MemberErrorCode.INVALID_NICKNAME)
		}
	}

	private fun member(email: String, name: String?, nickname: String): Member =
		Member.create(TestIdFactory.memberId(), Email(email), name, nickname)
}
