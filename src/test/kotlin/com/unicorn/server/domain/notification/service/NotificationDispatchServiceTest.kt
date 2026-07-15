package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.enums.NotificationEventType
import com.unicorn.server.domain.notification.enums.NotificationStatus
import com.unicorn.server.domain.notification.exception.PermanentNotificationSendException
import com.unicorn.server.domain.notification.exception.RetryableNotificationSendException
import com.unicorn.server.domain.notification.port.dto.NotificationMessage
import com.unicorn.server.domain.notification.port.out.NotificationMessageComposer
import com.unicorn.server.domain.notification.port.out.NotificationSender
import com.unicorn.server.domain.notification.port.out.NotificationOutPort
import com.unicorn.server.infrastructure.config.NotificationProperties
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("NotificationDispatchService 단위 테스트")
class NotificationDispatchServiceTest {
	private val notificationOutPort = FakeNotificationOutPort()
	private val successComposer = FakeComposer()
	private val successSender = RecordingSender()
	private val notificationProperties = NotificationProperties().apply {
		dispatch.maxAttempts = 3
		dispatch.baseRetryDelayMinutes = 5
	}
	private val notificationDispatchService = NotificationDispatchService(
		notificationOutPort = notificationOutPort,
		composers = listOf(successComposer),
		senders = listOf(successSender),
		notificationProperties = notificationProperties,
	)

	@Test
	@DisplayName("READY 알림 발송 성공 시 SENT 상태가 된다")
	fun dispatch_readyNotification_sent() {
		val notification = notificationOutPort.save(createNotification())

		notificationDispatchService.dispatch(10)

		val saved = notificationOutPort.findByDedupKey(notification.dedupKey)
		assertThat(saved?.status).isEqualTo(NotificationStatus.SENT)
		assertThat(successSender.messages).hasSize(1)
	}

	@Test
	@DisplayName("재시도 가능한 발송 실패 시 FAILED 상태와 nextRetryAt이 기록된다")
	fun dispatch_retryableFailure_failed() {
		val outPort = FakeNotificationOutPort()
		val service = NotificationDispatchService(
			notificationOutPort = outPort,
			composers = listOf(FakeComposer()),
			senders = listOf(RetryableFailSender()),
			notificationProperties = NotificationProperties().apply {
				dispatch.maxAttempts = 3
				dispatch.baseRetryDelayMinutes = 5
			},
		)
		val notification = outPort.save(createNotification(dedupKey = "retryable-key"))

		service.dispatch(10)

		val saved = outPort.findByDedupKey(notification.dedupKey)
		assertThat(saved?.status).isEqualTo(NotificationStatus.FAILED)
		assertThat(saved?.nextRetryAt).isNotNull()
	}

	@Test
	@DisplayName("영구 실패 시 DEAD 상태가 된다")
	fun dispatch_permanentFailure_dead() {
		val outPort = FakeNotificationOutPort()
		val service = NotificationDispatchService(
			notificationOutPort = outPort,
			composers = listOf(FakeComposer()),
			senders = listOf(PermanentFailSender()),
			notificationProperties = NotificationProperties().apply {
				dispatch.maxAttempts = 3
				dispatch.baseRetryDelayMinutes = 5
			},
		)
		val notification = outPort.save(createNotification(dedupKey = "dead-key"))

		service.dispatch(10)

		val saved = outPort.findByDedupKey(notification.dedupKey)
		assertThat(saved?.status).isEqualTo(NotificationStatus.DEAD)
	}

	private fun createNotification(dedupKey: String = "signup-1"): Notification = Notification.create(
		channel = NotificationChannel.PUSH,
		receiver = "token-1",
		eventType = NotificationEventType.SIGNUP_COMPLETED,
		payload = mapOf("title" to "가입 완료", "body" to "회원가입이 완료되었습니다."),
		dedupKey = dedupKey,
	)

	private class FakeNotificationOutPort : NotificationOutPort {
		private val notifications = linkedMapOf<String, Notification>()

		override fun save(notification: Notification): Notification {
			notifications[notification.dedupKey] = notification
			return notification
		}

		override fun findByDedupKey(dedupKey: String): Notification? = notifications[dedupKey]

		override fun findDispatchTargets(limit: Int, now: LocalDateTime): List<Notification> =
			notifications.values.filter { it.isDispatchable(now) }.take(limit)
	}

	private class FakeComposer : NotificationMessageComposer {
		override fun channel(): NotificationChannel = NotificationChannel.PUSH

		override fun compose(notification: Notification): NotificationMessage =
			object : NotificationMessage {
				override val channel: NotificationChannel = NotificationChannel.PUSH
			}
	}

	private class RecordingSender : NotificationSender {
		val messages = mutableListOf<NotificationMessage>()

		override fun channel(): NotificationChannel = NotificationChannel.PUSH

		override fun send(message: NotificationMessage) {
			messages += message
		}
	}

	private class RetryableFailSender : NotificationSender {
		override fun channel(): NotificationChannel = NotificationChannel.PUSH

		override fun send(message: NotificationMessage) {
			throw RetryableNotificationSendException("temporary provider error")
		}
	}

	private class PermanentFailSender : NotificationSender {
		override fun channel(): NotificationChannel = NotificationChannel.PUSH

		override fun send(message: NotificationMessage) {
			throw PermanentNotificationSendException("invalid token")
		}
	}
}
