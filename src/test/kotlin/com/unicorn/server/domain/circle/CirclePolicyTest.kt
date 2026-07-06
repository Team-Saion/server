package com.unicorn.server.domain.circle

import com.unicorn.server.TestIdFactory
import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.domain.member.vo.MemberId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Circle/CircleMember 정책 단위 테스트")
class CirclePolicyTest {
	@Test
	@DisplayName("유효한 써클 이름으로 생성 시 trim된 이름이 저장된다")
	fun create_withValidName_success() {
		val circle = Circle.create(TestIdFactory.circleId(), " 비니abc123 ", TestIdFactory.memberId())

		assertThat(circle.name).isEqualTo("비니abc123")
	}

	@Test
	@DisplayName("특수문자가 포함된 써클 이름으로 생성 시 예외가 발생한다")
	fun create_withInvalidCharset_throwsException() {
		assertThatThrownBy { Circle.create(TestIdFactory.circleId(), "비니네!", TestIdFactory.memberId()) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.CIRCLE_NAME_INVALID_CHARSET)
	}

	@Test
	@DisplayName("공백만 있는 써클 이름으로 생성 시 예외가 발생한다")
	fun create_withBlankName_throwsException() {
		assertThatThrownBy { Circle.create(TestIdFactory.circleId(), "   ", TestIdFactory.memberId()) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.CIRCLE_NAME_BLANK)
	}

	@Test
	@DisplayName("30자를 초과하는 써클 멤버 닉네임으로 생성 시 예외가 발생한다")
	fun createMember_withTooLongNickname_throwsException() {
		assertThatThrownBy {
			CircleMember.createMember(
				id = TestIdFactory.circleMemberId(),
				circleId = TestIdFactory.circleId(),
				memberId = TestIdFactory.memberId(),
				nickname = "a".repeat(31),
			)
		}
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.CIRCLE_NICKNAME_INVALID)
	}
}
