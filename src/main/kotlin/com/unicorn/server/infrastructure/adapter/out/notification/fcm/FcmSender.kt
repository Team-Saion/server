package com.unicorn.server.infrastructure.adapter.out.notification.fcm

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.MessagingErrorCode
import com.google.firebase.messaging.Notification
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.exception.PermanentNotificationSendException
import com.unicorn.server.domain.notification.exception.RetryableNotificationSendException
import com.unicorn.server.domain.notification.port.dto.NotificationMessage
import com.unicorn.server.domain.notification.port.out.NotificationSender
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "app.notification.fcm", name = ["enabled"], havingValue = "true")
class FcmSender(
	private val firebaseMessaging: FirebaseMessaging,
) : NotificationSender {
	override fun channel(): NotificationChannel = NotificationChannel.PUSH

	override fun send(message: NotificationMessage) {
		val fcmMessage = message as? FcmNotificationMessage
			?: throw PermanentNotificationSendException("Unsupported FCM notification message type")

		val firebaseMessage = Message.builder()
			.setToken(fcmMessage.token)
			.setNotification(
				Notification.builder()
					.setTitle(fcmMessage.title)
					.setBody(fcmMessage.body)
					.build(),
			)
			.putAllData(fcmMessage.data)
			.build()

		try {
			firebaseMessaging.send(firebaseMessage)
		} catch (e: FirebaseMessagingException) {
			handleFirebaseException(e)
		}
	}

	private fun handleFirebaseException(e: FirebaseMessagingException): Nothing {
		when (e.messagingErrorCode) {
			MessagingErrorCode.UNREGISTERED,
			MessagingErrorCode.INVALID_ARGUMENT,
			MessagingErrorCode.SENDER_ID_MISMATCH,
			-> throw PermanentNotificationSendException(e.message ?: "Permanent FCM failure", e)

			else -> throw RetryableNotificationSendException(e.message ?: "Retryable FCM failure", e)
		}
	}
}
