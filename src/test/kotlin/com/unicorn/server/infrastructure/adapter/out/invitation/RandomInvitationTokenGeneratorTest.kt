package com.unicorn.server.infrastructure.adapter.out.invitation

import com.unicorn.server.domain.invitation.vo.InvitationToken
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("RandomInvitationTokenGenerator 단위 테스트")
class RandomInvitationTokenGeneratorTest {
	private val generator = RandomInvitationTokenGenerator()

	@Test
	@DisplayName("6글자 초대장 코드를 생성한다")
	fun generate_returnsSixCharacterToken() {
		val token = generator.generate()

		assertThat(token.value).hasSize(InvitationToken.LENGTH)
	}
}
