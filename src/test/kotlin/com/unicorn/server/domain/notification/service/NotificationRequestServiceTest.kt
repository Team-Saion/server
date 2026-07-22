package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.NotificationTemplate
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.enums.NotificationEventType
import com.unicorn.server.domain.notification.event.ScheduleCreatedPayload
import com.unicorn.server.domain.notification.port.dto.RequestNotificationCommand
import com.unicorn.server.domain.notification.port.out.NotificationOutPort
import com.unicorn.server.domain.notification.port.out.NotificationTemplateOutPort
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("NotificationRequestService 단위 테스트")
class NotificationRequestServiceTest {
	private val notificationOutPort = FakeNotificationOutPort()
	private val notificationTemplateOutPort = FakeNotificationTemplateOutPort()
	private val notificationRequestService = NotificationRequestService(notificationOutPort, notificationTemplateOutPort)

	@Test
	@DisplayName("동일 dedup key 알림은 한 번만 저장된다")
	fun request_duplicateDedupKey_savedOnce() {
		val command = RequestNotificationCommand(
			channel = NotificationChannel.PUSH,
			receiver = "token-1",
			payload = ScheduleCreatedPayload(actorName = "민수", scheduleTitle = "병원 방문"),
			dedupKey = "schedule-created-1",
		)

		notificationRequestService.request(command)
		notificationRequestService.request(command)

		assertThat(notificationOutPort.notifications).hasSize(1)
		assertThat(notificationOutPort.notifications.getValue(command.dedupKey).payload)
			.containsEntry("title", "새 일정이 등록됐어요")
			.containsEntry("body", "민수님이 '병원 방문' 일정을 추가했어요.")
	}

	@Test
	@DisplayName("DB 템플릿 변수와 payload 계약이 다르면 알림을 저장하지 않는다")
	fun request_templateVariablesMismatch_doesNotSave() {
		val service = NotificationRequestService(
			notificationOutPort,
			FakeNotificationTemplateOutPort(bodyTemplate = "{actor_nmae}님이 '{schedule_title}' 일정을 추가했어요."),
		)
		val command = RequestNotificationCommand(
			channel = NotificationChannel.PUSH,
			receiver = "token-1",
			payload = ScheduleCreatedPayload(actorName = "민수", scheduleTitle = "병원 방문"),
			dedupKey = "schedule-created-2",
		)

		assertThatThrownBy { service.request(command) }
			.isInstanceOf(IllegalArgumentException::class.java)
			.hasMessageContaining("actor_nmae")
		assertThat(notificationOutPort.notifications).isEmpty()
	}

	private class FakeNotificationOutPort : NotificationOutPort {
		val notifications = linkedMapOf<String, Notification>()

		override fun save(notification: Notification): Notification {
			notifications[notification.dedupKey] = notification
			return notification
		}

		override fun findByDedupKey(dedupKey: String): Notification? = notifications[dedupKey]

		override fun findDispatchTargets(limit: Int, now: LocalDateTime): List<Notification> = emptyList()
	}

	private class FakeNotificationTemplateOutPort(
		private val bodyTemplate: String = "{actor_name}님이 '{schedule_title}' 일정을 추가했어요.",
	) : NotificationTemplateOutPort {
		override fun findActiveByEventType(eventType: NotificationEventType): NotificationTemplate? =
			NotificationTemplate(
				eventType = NotificationEventType.SCHEDULE_CREATED,
				titleTemplate = "새 일정이 등록됐어요",
				bodyTemplate = bodyTemplate,
			).takeIf { it.eventType == eventType }
	}
}
