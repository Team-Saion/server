package com.unicorn.server.infrastructure.adapter.out.notification.fcm

import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.exception.PermanentNotificationSendException
import com.unicorn.server.domain.notification.port.dto.NotificationMessage
import com.unicorn.server.domain.notification.port.out.NotificationMessageComposer
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "app.notification.fcm", name = ["enabled"], havingValue = "true")
class FcmMessageComposer : NotificationMessageComposer {
	override fun channel(): NotificationChannel = NotificationChannel.PUSH

	override fun compose(notification: Notification): NotificationMessage {
		val title = notification.payload[KEY_TITLE]
			?: throw PermanentNotificationSendException("Missing FCM title payload")
		val body = notification.payload[KEY_BODY]
			?: throw PermanentNotificationSendException("Missing FCM body payload")
		val data = notification.payload
			.filterKeys { it != KEY_TITLE && it != KEY_BODY }
			.plus(KEY_EVENT_TYPE to notification.eventType.name)

		return FcmNotificationMessage(
			token = notification.receiver,
			title = title,
			body = body,
			data = data,
		)
	}

	companion object {
		private const val KEY_TITLE = "title"
		private const val KEY_BODY = "body"
		private const val KEY_EVENT_TYPE = "eventType"
	}
}
