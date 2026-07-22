package com.unicorn.server.infrastructure.adapter.`in`.event.invitation

import com.unicorn.server.common.domain.Event
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.circle.port.dto.CircleMemberDto
import com.unicorn.server.domain.circle.port.dto.CircleSummary
import com.unicorn.server.domain.circle.port.dto.JoinCircleResult
import com.unicorn.server.domain.invitation.enums.InvitationType
import com.unicorn.server.domain.invitation.event.InvitationRedeemedEvent
import com.unicorn.server.domain.notification.DevicePushToken
import com.unicorn.server.domain.notification.enums.DevicePlatform
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.enums.NotificationEventType
import com.unicorn.server.domain.notification.event.NotificationRequestedEvent
import com.unicorn.server.domain.notification.port.`in`.NotificationPushTokenInPort
import com.unicorn.server.domain.notification.port.dto.RegisterPushTokenCommand
import com.unicorn.server.domain.notification.vo.DevicePushTokenId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("InvitationNotificationEventListener 단위 테스트")
class InvitationNotificationEventListenerTest {
	@Test
	@DisplayName("초대 수락 완료 시 참여자를 제외한 활성 써클 멤버의 모든 푸시 토큰에 알림을 요청한다")
	fun handle_invitationRedeemed_requestsPushForOtherActiveCircleMembers() {
		val circleMemberInPort = FakeCircleMemberInPort(
			listOf(
				member("owner"),
				member("family"),
				member("invitee"),
				member("inactive", active = false),
			),
		)
		val pushTokenInPort = FakeNotificationPushTokenInPort(
			mapOf(
				"owner" to listOf(pushToken(1, "owner-token")),
				"family" to listOf(pushToken(2, "family-token-1"), pushToken(3, "family-token-2")),
				"invitee" to listOf(pushToken(4, "invitee-token")),
				"inactive" to listOf(pushToken(5, "inactive-token")),
			),
		)
		val eventPublisher = RecordingEventPublisher()
		val listener = InvitationNotificationEventListener(
			circleMemberInPort,
			pushTokenInPort,
			eventPublisher,
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

		val events = eventPublisher.events.filterIsInstance<NotificationRequestedEvent>()
		assertThat(events)
			.extracting<String> { it.receiver }
			.containsExactlyInAnyOrder("owner-token", "family-token-1", "family-token-2")
		assertThat(events).allSatisfy { notificationEvent ->
			assertThat(notificationEvent.channel).isEqualTo(NotificationChannel.PUSH)
			assertThat(notificationEvent.payload.eventType).isEqualTo(NotificationEventType.CIRCLE_JOIN_COMPLETED)
			assertThat(notificationEvent.payload.toVariables())
				.containsEntry("member_name", "유니콘")
				.containsEntry("circle_name", "우리 가족")
		}
		assertThat(events.map { it.dedupKey }).doesNotHaveDuplicates()
	}

	private fun member(memberId: String, active: Boolean = true) =
		CircleMemberDto(memberId, memberId, "MEMBER", active)

	private fun pushToken(id: Long, token: String): DevicePushToken {
		val now = LocalDateTime.now()
		return DevicePushToken.reconstitute(
			id = DevicePushTokenId.of(id),
			memberId = "member-$id",
			token = token,
			platform = DevicePlatform.IOS,
			osNotificationPermissionGranted = true,
			appVersion = null,
			active = true,
			lastSeenAt = now,
			invalidatedAt = null,
			createdAt = now,
			updatedAt = now,
		)
	}

	private class FakeCircleMemberInPort(
		private val members: List<CircleMemberDto>,
	) : CircleMemberInPort {
		override fun getCircleMembers(circleId: String): List<CircleMemberDto> = members
		override fun join(circleId: String, memberId: String): JoinCircleResult = error("not used")
		override fun leave(circleId: String, memberId: String) = error("not used")
		override fun isCircleMember(circleId: String, memberId: String): Boolean = error("not used")
		override fun transferInitiator(circleId: String, currentInitiatorId: String, newInitiatorId: String): CircleSummary = error("not used")
		override fun handleMemberWithdrawal(memberId: String) = error("not used")
	}

	private class FakeNotificationPushTokenInPort(
		private val tokensByMemberId: Map<String, List<DevicePushToken>>,
	) : NotificationPushTokenInPort {
		override fun getActiveReceivable(memberId: String): List<DevicePushToken> = tokensByMemberId[memberId].orEmpty()
		override fun register(memberId: String, command: RegisterPushTokenCommand): DevicePushToken = error("not used")
		override fun deactivate(memberId: String, tokenId: Long) = error("not used")
	}

	private class RecordingEventPublisher : EventPublisher {
		val events = mutableListOf<Event>()

		override fun publish(event: Event) {
			events += event
		}
	}
}
