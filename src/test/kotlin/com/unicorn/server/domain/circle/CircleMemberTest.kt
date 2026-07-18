package com.unicorn.server.domain.circle

import com.unicorn.server.TestIdFactory
import com.unicorn.server.domain.circle.enums.CircleMemberStatus
import com.unicorn.server.domain.member.vo.MemberId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CircleMember 단위 테스트")
class CircleMemberTest {
	@Test
	@DisplayName("탈퇴하면 멤버십이 LEFT 상태로 soft delete 된다")
	fun leave_softDeletesMembership() {
		val circleMember = createMember()

		circleMember.leave()

		assertThat(circleMember.status).isEqualTo(CircleMemberStatus.LEFT)
		assertThat(circleMember.deleted).isTrue()
		assertThat(circleMember.leftAt).isNotNull()
	}

	@Test
	@DisplayName("탈퇴한 멤버가 재가입하면 기존 멤버십이 복원된다")
	fun rejoin_restoresSoftDeletedMembership() {
		val circleMember = createMember()
		circleMember.leave()

		circleMember.rejoin("새닉네임")

		assertThat(circleMember.status).isEqualTo(CircleMemberStatus.ACTIVE)
		assertThat(circleMember.deleted).isFalse()
		assertThat(circleMember.leftAt).isNull()
		assertThat(circleMember.nickname).isEqualTo("새닉네임")
	}

	private fun createMember(): CircleMember =
		CircleMember.createMember(
			id = TestIdFactory.circleMemberId(),
			circleId = TestIdFactory.circleId(),
			memberId = MemberId.generate(),
			nickname = "멤버",
		)
}
