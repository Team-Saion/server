package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.exception.ExpiredPushTokenException
import com.unicorn.server.domain.notification.exception.PermanentNotificationSendException
import com.unicorn.server.domain.notification.exception.RetryableNotificationSendException
import com.unicorn.server.domain.notification.port.`in`.NotificationDispatchInPort
import com.unicorn.server.domain.notification.port.out.NotificationMessageComposeOutPort
import com.unicorn.server.domain.notification.port.out.NotificationOutPort
import com.unicorn.server.domain.notification.port.out.NotificationPushTokenOutPort
import com.unicorn.server.domain.notification.port.out.NotificationSendOutPort
import com.unicorn.server.infrastructure.config.NotificationProperties
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class NotificationDispatchService(
    private val notificationOutPort: NotificationOutPort,
    private val notificationPushTokenOutPort: NotificationPushTokenOutPort,
    composers: List<NotificationMessageComposeOutPort>,
    senders: List<NotificationSendOutPort>,
    private val notificationProperties: NotificationProperties,
) : NotificationDispatchInPort {
    private val composerRegistry: Map<NotificationChannel, NotificationMessageComposeOutPort> =
        composers.associateBy { it.channel() }
    private val senderRegistry: Map<NotificationChannel, NotificationSendOutPort> = senders.associateBy { it.channel() }

    override fun dispatch(limit: Int) {
        val now = LocalDateTime.now()
        val notifications = notificationOutPort.claimDispatchTargets(limit, now)

        notifications.forEach { dispatchSingle(it, now) }
    }

    private fun dispatchSingle(notification: Notification, now: LocalDateTime) {
        val composer = composerRegistry[notification.channel]
        val sender = senderRegistry[notification.channel]

        if (composer == null || sender == null) {
            notification.markDead("No composer or sender configured for channel=${notification.channel}", now)
            notificationOutPort.save(notification)
            return
        }

        try {
            val message = composer.compose(notification)
            sender.send(message)
            notification.markSent(now)
        } catch (e: ExpiredPushTokenException) {
            notification.markDead(e.message ?: "Expired push token", now)
            notificationPushTokenOutPort.deleteByToken(notification.receiver)
        } catch (e: PermanentNotificationSendException) {
            notification.markDead(e.message ?: "Permanent send failure", now)
        } catch (e: RetryableNotificationSendException) {
            handleRetryableFailure(notification, e.message ?: "Retryable send failure", now)
        } catch (e: RuntimeException) {
            handleRetryableFailure(notification, e.message ?: "Unexpected send failure", now)
        }

        notificationOutPort.save(notification)
    }

    private fun handleRetryableFailure(notification: Notification, reason: String, now: LocalDateTime) {
        if (notification.attemptCount >= notificationProperties.dispatch.maxAttempts) {
            notification.markDead(reason, now)
            return
        }

        val retryAt = now.plusMinutes(
            notificationProperties.dispatch.baseRetryDelayMinutes * notification.attemptCount.toLong(),
        )
        notification.markFailed(reason, retryAt, now)
    }
}
