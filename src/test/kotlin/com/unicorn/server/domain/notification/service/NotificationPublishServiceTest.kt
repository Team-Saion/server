package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.DevicePushToken
import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.NotificationInboxItem
import com.unicorn.server.domain.notification.NotificationSetting
import com.unicorn.server.domain.notification.enums.DevicePlatform
import com.unicorn.server.domain.notification.enums.NotificationType
import com.unicorn.server.domain.notification.event.CircleJoinCompletedPayload
import com.unicorn.server.domain.notification.event.ScheduleCreatedPayload
import com.unicorn.server.domain.notification.event.ScheduleReminderDDayTimedPayload
import com.unicorn.server.domain.notification.event.ScheduleReminderD7Payload
import com.unicorn.server.domain.notification.port.`in`.NotificationPushTokenInPort
import com.unicorn.server.domain.notification.port.`in`.NotificationSettingInPort
import com.unicorn.server.domain.notification.port.dto.RegisterPushTokenCommand
import com.unicorn.server.domain.notification.port.dto.PublishNotificationCommand
import com.unicorn.server.domain.notification.port.dto.UpdateNotificationSettingCommand
import com.unicorn.server.domain.notification.port.out.NotificationInboxOutPort
import com.unicorn.server.domain.notification.port.out.NotificationOutPort
import com.unicorn.server.domain.notification.vo.DevicePushTokenId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("NotificationPublishService 단위 테스트")
class NotificationPublishServiceTest {
	@Test
	@DisplayName("써클 참여 알림은 보관함과 활성 토큰별 푸시 발송 작업을 생성한다")
	fun publish_circleJoin_createsInboxAndPush() {
		val fixture = Fixture()

		fixture.service.publish(
			PublishNotificationCommand(
				receiverMemberId = "member-1",
				payload = CircleJoinCompletedPayload("민수", "우리 가족"),
				eventId = "invitation-1",
				circleId = "circle-1",
			),
		)

		val item = fixture.inboxOutPort.items.single()
		assertThat(item.receiverMemberId).isEqualTo("member-1")
		assertThat(item.type).isEqualTo(NotificationType.CIRCLE_JOIN_COMPLETED)
		assertThat(item.title).isEqualTo("새 가족이 참여했어요")
		assertThat(item.route.circleId).isEqualTo("circle-1")
		assertThat(fixture.notificationOutPort.notifications).hasSize(1)
	}

	@Test
	@DisplayName("일정 생성 알림은 보관함 하나와 활성 토큰별 푸시 작업을 생성한다")
	fun publish_scheduleCreated_createsOneInboxAndPushPerToken() {
		val fixture = Fixture(tokens = listOf(pushToken(1), pushToken(2)))
		val command = PublishNotificationCommand(
			receiverMemberId = "member-1",
			payload = ScheduleCreatedPayload("민수", "병원 방문", "schedule-1"),
			eventId = "schedule-1",
			circleId = "circle-1",
			scheduleId = "schedule-1",
		)

		fixture.service.publish(command)
		fixture.service.publish(command)

		assertThat(fixture.inboxOutPort.items).hasSize(1)
		assertThat(fixture.notificationOutPort.notifications).hasSize(2)
		assertThat(fixture.notificationOutPort.notifications.values)
			.extracting<String> { it.receiver }
			.containsExactlyInAnyOrder("token-1", "token-2")
		assertThat(fixture.notificationOutPort.notifications.keys)
			.allMatch { it.startsWith("schedule_created:schedule-1:member-1:token:") }
		assertThat(fixture.notificationOutPort.notifications.values)
			.allSatisfy { notification ->
				assertThat(notification.payload).containsEntry("schedule_id", "schedule-1")
			}
	}

	@Test
	@DisplayName("D-day 시간 지정 리마인더 푸시 payload에는 일정 ID가 포함된다")
	fun publish_dDayTimedReminder_includesScheduleIdInPushPayload() {
		val fixture = Fixture()

		fixture.service.publish(
			PublishNotificationCommand(
				receiverMemberId = "member-1",
				payload = ScheduleReminderDDayTimedPayload("병원 방문", "09:00", "schedule-1"),
				eventId = "dday-timed:schedule-1",
				circleId = "circle-1",
				scheduleId = "schedule-1",
			),
		)

		val notification = fixture.notificationOutPort.notifications.values.single()
		assertThat(notification.payload).containsEntry("schedule_id", "schedule-1")
	}

	@Test
	@DisplayName("푸시 설정이 꺼져 있어도 보관함은 생성하고 푸시 작업은 생성하지 않는다")
	fun publish_pushSettingDisabled_createsInboxWithoutPush() {
		val fixture = Fixture(pushEnabled = false)

		fixture.service.publish(
			PublishNotificationCommand(
				receiverMemberId = "member-1",
				payload = ScheduleReminderD7Payload("병원 방문", "schedule-1"),
				eventId = "d7:schedule-1",
				circleId = "circle-1",
				scheduleId = "schedule-1",
			),
		)

		assertThat(fixture.inboxOutPort.items).hasSize(1)
		assertThat(fixture.notificationOutPort.notifications).isEmpty()
	}

	private class Fixture(
		tokens: List<DevicePushToken> = listOf(pushToken(1)),
		pushEnabled: Boolean = true,
	) {
		val notificationOutPort = FakeNotificationOutPort()
		val inboxOutPort = FakeNotificationInboxOutPort()
		val service = NotificationPublishService(
			notificationOutPort = notificationOutPort,
			notificationInboxOutPort = inboxOutPort,
			notificationPushTokenInPort = FakeNotificationPushTokenInPort(tokens),
			notificationSettingInPort = FakeNotificationSettingInPort(pushEnabled),
		)
	}

	private class FakeNotificationOutPort : NotificationOutPort {
		val notifications = linkedMapOf<String, Notification>()

		override fun save(notification: Notification): Notification {
			notifications[notification.dedupKey] = notification
			return notification
		}

		override fun findByDedupKey(dedupKey: String): Notification? = notifications[dedupKey]
		override fun claimDispatchTargets(
			limit: Int,
			now: LocalDateTime,
		): List<Notification> = emptyList()
	}

	private class FakeNotificationInboxOutPort : NotificationInboxOutPort {
		val items = mutableListOf<NotificationInboxItem>()

		override fun save(item: NotificationInboxItem): NotificationInboxItem {
			items += item
			return item
		}

		override fun findByDedupKey(dedupKey: String): NotificationInboxItem? =
			items.firstOrNull { it.dedupKey == dedupKey }

		override fun findPageByReceiver(memberId: String, cursor: Long?, limit: Int) = emptyList<NotificationInboxItem>()
		override fun findByIdAndReceiver(notificationId: Long, memberId: String): NotificationInboxItem? = null
		override fun deleteCreatedBefore(threshold: LocalDateTime): Int = 0
	}

	private class FakeNotificationPushTokenInPort(
		private val tokens: List<DevicePushToken>,
	) : NotificationPushTokenInPort {
		override fun getActiveReceivable(memberId: String): List<DevicePushToken> = tokens
		override fun register(memberId: String, command: RegisterPushTokenCommand): DevicePushToken = error("not used")
		override fun deactivate(memberId: String, tokenId: Long) = error("not used")
	}

	private class FakeNotificationSettingInPort(
		private val pushEnabled: Boolean,
	) : NotificationSettingInPort {
		override fun getSetting(memberId: String): NotificationSetting {
			val now = LocalDateTime.now()
			return NotificationSetting.reconstitute(
				memberId = memberId,
				d7Enabled = pushEnabled,
				d1Enabled = pushEnabled,
				dDayEnabled = pushEnabled,
				familyScheduleCheckEnabled = pushEnabled,
				createdAt = now,
				updatedAt = now,
			)
		}
		override fun updateSetting(memberId: String, command: UpdateNotificationSettingCommand): NotificationSetting =
			error("not used")
	}

	companion object {
		private fun pushToken(id: Long): DevicePushToken {
			val now = LocalDateTime.now()
			return DevicePushToken.reconstitute(
				id = DevicePushTokenId.of(id),
				memberId = "member-1",
				installationId = "installation-$id",
				token = "token-$id",
				platform = DevicePlatform.IOS,
				active = true,
				lastSeenAt = now,
				invalidatedAt = null,
				createdAt = now,
				updatedAt = now,
			)
		}
	}
}
