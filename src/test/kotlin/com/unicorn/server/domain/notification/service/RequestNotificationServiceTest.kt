package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.enums.NotificationEventType
import com.unicorn.server.domain.notification.port.dto.RequestNotificationCommand
import com.unicorn.server.domain.notification.port.out.NotificationStore
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("RequestNotificationService 단위 테스트")
class RequestNotificationServiceTest {
	private val notificationStore = FakeNotificationStore()
	private val requestNotificationService = RequestNotificationService(notificationStore)

	@Test
	@DisplayName("동일 dedup key 알림은 한 번만 저장된다")
	fun request_duplicateDedupKey_savedOnce() {
		val command = RequestNotificationCommand(
			channel = NotificationChannel.PUSH,
			receiver = "token-1",
			eventType = NotificationEventType.SIGNUP_COMPLETED,
			payload = mapOf("title" to "가입 완료", "body" to "회원가입이 완료되었습니다."),
			dedupKey = "signup-1",
		)

		requestNotificationService.request(command)
		requestNotificationService.request(command)

		assertThat(notificationStore.notifications).hasSize(1)
	}

	private class FakeNotificationStore : NotificationStore {
		val notifications = linkedMapOf<String, Notification>()

		override fun save(notification: Notification): Notification {
			notifications[notification.dedupKey] = notification
			return notification
		}

		override fun findByDedupKey(dedupKey: String): Notification? = notifications[dedupKey]

		override fun findDispatchTargets(limit: Int, now: LocalDateTime): List<Notification> = emptyList()
	}
}
