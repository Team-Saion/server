package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.exception.PermanentNotificationSendException
import com.unicorn.server.domain.notification.exception.RetryableNotificationSendException
import com.unicorn.server.domain.notification.port.`in`.DispatchReadyNotificationsInPort
import com.unicorn.server.domain.notification.port.out.NotificationMessageComposer
import com.unicorn.server.domain.notification.port.out.NotificationSender
import com.unicorn.server.domain.notification.port.out.NotificationStore
import com.unicorn.server.infrastructure.config.NotificationProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class NotificationDispatchService(
	private val notificationStore: NotificationStore,
	composers: List<NotificationMessageComposer>,
	senders: List<NotificationSender>,
	private val notificationProperties: NotificationProperties,
) : DispatchReadyNotificationsInPort {
	private val composerRegistry: Map<*, *> = composers.associateBy { it.channel() }
	private val senderRegistry: Map<*, *> = senders.associateBy { it.channel() }

	@Transactional
	override fun dispatch(limit: Int) {
		val now = LocalDateTime.now()
		val notifications = notificationStore.findDispatchTargets(limit, now)

		notifications.forEach { dispatchSingle(it, now) }
	}

	private fun dispatchSingle(notification: Notification, now: LocalDateTime) {
		val composer = composerRegistry[notification.channel] as? NotificationMessageComposer
		val sender = senderRegistry[notification.channel] as? NotificationSender

		notification.markProcessing(now)
		notificationStore.save(notification)

		if (composer == null || sender == null) {
			notification.markDead("No composer or sender configured for channel=${notification.channel}", now)
			notificationStore.save(notification)
			return
		}

		try {
			val message = composer.compose(notification)
			sender.send(message)
			notification.markSent(now)
		} catch (e: PermanentNotificationSendException) {
			notification.markDead(e.message ?: "Permanent send failure", now)
		} catch (e: RetryableNotificationSendException) {
			handleRetryableFailure(notification, e.message ?: "Retryable send failure", now)
		} catch (e: RuntimeException) {
			handleRetryableFailure(notification, e.message ?: "Unexpected send failure", now)
		}

		notificationStore.save(notification)
	}

	private fun handleRetryableFailure(notification: Notification, reason: String, now: LocalDateTime) {
		val maxAttempts = notificationProperties.dispatch.maxAttempts
		if (notification.attemptCount >= maxAttempts) {
			notification.markDead(reason, now)
			return
		}

		val baseRetryDelayMinutes = notificationProperties.dispatch.baseRetryDelayMinutes
		val retryAt = now.plusMinutes(baseRetryDelayMinutes * notification.attemptCount.toLong())
		notification.markFailed(reason, retryAt, now)
	}
}
