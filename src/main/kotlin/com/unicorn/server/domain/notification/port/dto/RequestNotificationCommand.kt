package com.unicorn.server.domain.notification.port.dto

import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.event.NotificationEventPayload

data class RequestNotificationCommand(
	val channel: NotificationChannel,
	val receiver: String,
	val payload: NotificationEventPayload,
	val dedupKey: String,
)
