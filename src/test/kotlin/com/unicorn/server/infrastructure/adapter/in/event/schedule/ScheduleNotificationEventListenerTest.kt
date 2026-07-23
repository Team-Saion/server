package com.unicorn.server.infrastructure.adapter.`in`.event.schedule

import com.unicorn.server.common.domain.Event
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.circle.port.dto.CircleMemberDto
import com.unicorn.server.domain.circle.port.dto.CircleSummary
import com.unicorn.server.domain.circle.port.dto.JoinCircleResult
import com.unicorn.server.domain.notification.DevicePushToken
import com.unicorn.server.domain.notification.NotificationSetting
import com.unicorn.server.domain.notification.enums.DevicePlatform
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.enums.NotificationEventType
import com.unicorn.server.domain.notification.event.NotificationRequestedEvent
import com.unicorn.server.domain.notification.event.ScheduleCreatedPayload
import com.unicorn.server.domain.notification.port.`in`.NotificationPushTokenInPort
import com.unicorn.server.domain.notification.port.`in`.NotificationSettingInPort
import com.unicorn.server.domain.notification.port.dto.RegisterPushTokenCommand
import com.unicorn.server.domain.notification.port.dto.UpdateNotificationSettingCommand
import com.unicorn.server.domain.notification.vo.DevicePushTokenId
import com.unicorn.server.domain.schedule.event.ScheduleCreatedEvent
import com.unicorn.server.domain.schedule.enums.ScheduleReminderType
import com.unicorn.server.domain.schedule.event.ScheduleReminderDueEvent
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
		val listener = ScheduleNotificationEventListener(
			circleMemberInPort,
			notificationPushTokenInPort,
			FakeNotificationSettingInPort(),
			eventPublisher,
		)

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

	@Test
	@DisplayName("D-7 리마인드는 D-7 설정이 ON인 활성 구성원의 토큰에만 알림을 요청한다")
	fun handle_d7Reminder_requestsPushOnlyForMembersWithD7Enabled() {
		val circleMemberInPort = FakeCircleMemberInPort(
			listOf(
				CircleMemberDto("enabled", "수신", "MEMBER", true),
				CircleMemberDto("disabled", "미수신", "MEMBER", true),
				CircleMemberDto("inactive", "비활성", "MEMBER", false),
			),
		)
		val notificationPushTokenInPort = FakeNotificationPushTokenInPort(
			mapOf(
				"enabled" to listOf(pushToken(1, "enabled-token")),
				"disabled" to listOf(pushToken(2, "disabled-token")),
				"inactive" to listOf(pushToken(3, "inactive-token")),
			),
		)
		val eventPublisher = RecordingEventPublisher()
		val listener = ScheduleNotificationEventListener(
			circleMemberInPort,
			notificationPushTokenInPort,
			FakeNotificationSettingInPort(disabledMembers = setOf("disabled")),
			eventPublisher,
		)

		listener.handle(
			ScheduleReminderDueEvent(
				reminderType = ScheduleReminderType.D7,
				scheduleId = "SC1",
				circleId = "circle-1",
				scheduleTitle = "제주도 여행",
				startTime = null,
			),
		)

		val events = eventPublisher.events.filterIsInstance<NotificationRequestedEvent>()
		assertThat(events).singleElement().satisfies { event ->
			assertThat(event.receiver).isEqualTo("enabled-token")
			assertThat(event.channel).isEqualTo(NotificationChannel.PUSH)
			assertThat(event.payload.toVariables()).containsEntry("schedule_title", "제주도 여행")
			assertThat(event.dedupKey).isEqualTo("schedule-reminder:d7:SC1:enabled:token:1")
		}
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

	private class FakeNotificationSettingInPort(
		private val disabledMembers: Set<String> = emptySet(),
	) : NotificationSettingInPort {
		override fun getSetting(memberId: String): NotificationSetting {
			val enabled = memberId !in disabledMembers
			val now = LocalDateTime.now()
			return NotificationSetting.reconstitute(
				memberId = memberId,
				d7Enabled = enabled,
				d1Enabled = enabled,
				dDayEnabled = enabled,
				familyScheduleCheckEnabled = enabled,
				createdAt = now,
				updatedAt = now,
			)
		}

		override fun updateSetting(memberId: String, command: UpdateNotificationSettingCommand): NotificationSetting =
			error("not used")
	}

	private class RecordingEventPublisher : EventPublisher {
		val events = mutableListOf<Event>()

		override fun publish(event: Event) {
			events += event
		}
	}
}
