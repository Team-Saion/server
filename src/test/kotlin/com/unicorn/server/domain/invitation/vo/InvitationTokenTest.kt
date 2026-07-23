package com.unicorn.server.domain.invitation.vo

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.invitation.exception.InvitationErrorCode
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("InvitationToken 단위 테스트")
class InvitationTokenTest {
	@Test
	@DisplayName("허용된 문자로 구성된 6글자 초대장 코드를 생성할 수 있다")
	fun create_withSixAllowedCharacters_succeeds() {
		val token = InvitationToken("Ab12_-")

		assertThat(token.value).hasSize(6)
	}

	@Test
	@DisplayName("6글자가 아닌 초대장 코드는 유효하지 않다")
	fun create_withInvalidLength_throwsException() {
		assertThatThrownBy { InvitationToken("Ab12_") }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(InvitationErrorCode.INVITATION_TOKEN_INVALID)
	}
}
