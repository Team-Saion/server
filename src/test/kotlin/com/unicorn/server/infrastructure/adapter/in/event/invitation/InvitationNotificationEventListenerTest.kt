package com.unicorn.server.infrastructure.adapter.`in`.event.invitation

import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.circle.port.dto.CircleMemberDto
import com.unicorn.server.domain.circle.port.dto.CircleSummary
import com.unicorn.server.domain.circle.port.dto.JoinCircleResult
import com.unicorn.server.domain.invitation.enums.InvitationType
import com.unicorn.server.domain.invitation.event.InvitationRedeemedEvent
import com.unicorn.server.domain.notification.enums.NotificationType
import com.unicorn.server.domain.notification.port.`in`.NotificationRequestInPort
import com.unicorn.server.domain.notification.port.dto.RequestNotificationCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("InvitationNotificationEventListener 단위 테스트")
class InvitationNotificationEventListenerTest {
	@Test
	@DisplayName("초대 수락 완료 시 참여자를 제외한 활성 써클 멤버에게 알림을 요청한다")
	fun handle_invitationRedeemed_requestsNotificationForOtherActiveCircleMembers() {
		val notificationRequestInPort = RecordingNotificationRequestInPort()
		val listener = InvitationNotificationEventListener(
			FakeCircleMemberInPort(
				listOf(
					member("owner"),
					member("family"),
					member("invitee"),
					member("inactive", active = false),
				),
			),
			notificationRequestInPort,
		)

		listener.handle(
			InvitationRedeemedEvent(
				invitationId = "invitation-1",
				type = InvitationType.CIRCLE,
				targetId = "circle-1",
				inviterMemberId = "owner",
				redeemerMemberId = "invitee",
				circleName = "우리 가족",
				redeemerNickname = "유니콘",
			),
		)

		assertThat(notificationRequestInPort.commands)
			.extracting<String> { it.receiverMemberId }
			.containsExactlyInAnyOrder("owner", "family")
		assertThat(notificationRequestInPort.commands).allSatisfy { command ->
			assertThat(command.payload.type).isEqualTo(NotificationType.CIRCLE_JOIN_COMPLETED)
			assertThat(command.eventId).isEqualTo("invitation-1")
			assertThat(command.circleId).isEqualTo("circle-1")
		}
	}

	private fun member(memberId: String, active: Boolean = true) =
		CircleMemberDto(memberId, memberId, "MEMBER", active)

	private class RecordingNotificationRequestInPort : NotificationRequestInPort {
		val commands = mutableListOf<RequestNotificationCommand>()
		override fun request(command: RequestNotificationCommand) {
			commands += command
		}
	}

	private class FakeCircleMemberInPort(
		private val members: List<CircleMemberDto>,
	) : CircleMemberInPort {
		override fun getCircleMembers(circleId: String): List<CircleMemberDto> = members
		override fun join(circleId: String, memberId: String): JoinCircleResult = error("not used")
		override fun leave(circleId: String, memberId: String) = error("not used")
		override fun isCircleMember(circleId: String, memberId: String): Boolean = error("not used")
		override fun transferInitiator(circleId: String, currentInitiatorId: String, newInitiatorId: String): CircleSummary =
			error("not used")
		override fun handleMemberWithdrawal(memberId: String) = error("not used")
	}
}
