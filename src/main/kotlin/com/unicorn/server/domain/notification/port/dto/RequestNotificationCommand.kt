package com.unicorn.server.domain.notification.port.dto

import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.enums.NotificationEventType

data class RequestNotificationCommand(
	val channel: NotificationChannel,
	val receiver: String,
	val eventType: NotificationEventType,
	val payload: Map<String, String>,
	val dedupKey: String,
)
