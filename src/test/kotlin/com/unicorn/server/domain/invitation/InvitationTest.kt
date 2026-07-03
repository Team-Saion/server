package com.unicorn.server.domain.invitation

import com.unicorn.server.TestIdFactory
import com.unicorn.server.domain.invitation.enums.InvitationType
import com.unicorn.server.domain.invitation.exception.InvitationSelfApprovalForbiddenException
import com.unicorn.server.domain.invitation.vo.InvitationToken
import com.unicorn.server.domain.member.vo.MemberId
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
@DisplayName("Invitation 단위 테스트")
class InvitationTest {
	@Test
	@DisplayName("자신이 발급한 초대장을 자신이 수락하려 하면 예외가 발생한다")
	fun ensureNotSelfApproval_withInviter_throwsException() {
		val inviterId = TestIdFactory.memberId()
		val invitation = Invitation.create(
			id = TestIdFactory.invitationId(),
			type = InvitationType.CIRCLE,
			targetId = TestIdFactory.circleId().toString(),
			token = InvitationToken("abcdefghijklmnopqrstuvwxABCDEFGH"),
			inviterId = inviterId,
			inviteToName = null,
			message = null,
		)

		assertThatThrownBy { invitation.ensureNotSelfApproval(inviterId) }
			.isInstanceOf(InvitationSelfApprovalForbiddenException::class.java)
	}
}
