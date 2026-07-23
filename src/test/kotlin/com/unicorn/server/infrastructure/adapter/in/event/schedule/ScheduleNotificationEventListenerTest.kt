package com.unicorn.server.infrastructure.adapter.`in`.event.schedule

import com.unicorn.server.common.domain.Event
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.circle.port.dto.CircleMemberDto
import com.unicorn.server.domain.circle.port.dto.CircleSummary
import com.unicorn.server.domain.circle.port.dto.JoinCircleResult
import com.unicorn.server.domain.notification.DevicePushToken
import com.unicorn.server.domain.notification.enums.DevicePlatform
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.enums.NotificationEventType
import com.unicorn.server.domain.notification.event.NotificationRequestedEvent
import com.unicorn.server.domain.notification.event.ScheduleCreatedPayload
import com.unicorn.server.domain.notification.port.`in`.NotificationPushTokenInPort
import com.unicorn.server.domain.notification.port.dto.RegisterPushTokenCommand
import com.unicorn.server.domain.notification.vo.DevicePushTokenId
import com.unicorn.server.domain.schedule.event.ScheduleCreatedEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("ScheduleNotificationEventListener 단위 테스트")
class ScheduleNotificationEventListenerTest {
	@Test
	@DisplayName("일정 생성 시 작성자를 포함한 활성 구성원의 모든 활성 토큰에 기존 알림 요청 이벤트를 발행한다")
	fun handle_scheduleCreated_publishesNotificationRequestForEveryActiveToken() {
		val circleMemberInPort = FakeCircleMemberInPort(
			listOf(
				CircleMemberDto("creator", "유니콘", "MEMBER", true),
				CircleMemberDto("family", "가족", "MEMBER", true),
				CircleMemberDto("inactive", "비활성", "MEMBER", false),
			),
		)
		val notificationPushTokenInPort = FakeNotificationPushTokenInPort(
			mapOf(
				"creator" to listOf(pushToken(1, "creator-token")),
				"family" to listOf(pushToken(2, "family-token-1"), pushToken(3, "family-token-2")),
				"inactive" to listOf(pushToken(4, "inactive-token")),
			),
		)
		val eventPublisher = RecordingEventPublisher()
		val listener = ScheduleNotificationEventListener(circleMemberInPort, notificationPushTokenInPort, eventPublisher)

		listener.handle(
			ScheduleCreatedEvent(
				scheduleId = "SC1",
				circleId = "circle-1",
				creatorMemberId = "creator",
				scheduleTitle = "제주도 여행",
			),
		)

		val events = eventPublisher.events.filterIsInstance<NotificationRequestedEvent>()
		assertThat(events).extracting<String> { it.receiver }
			.containsExactlyInAnyOrder("creator-token", "family-token-1", "family-token-2")
		assertThat(events).allSatisfy { event ->
			assertThat(event.channel).isEqualTo(NotificationChannel.PUSH)
			assertThat(event.payload.eventType).isEqualTo(NotificationEventType.SCHEDULE_CREATED)
			assertThat(event.payload).isEqualTo(ScheduleCreatedPayload("유니콘", "제주도 여행"))
		}
		assertThat(events.map { it.dedupKey }).doesNotHaveDuplicates()
	}

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
