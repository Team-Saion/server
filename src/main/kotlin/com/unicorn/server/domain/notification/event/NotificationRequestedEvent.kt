package com.unicorn.server.domain.notification.event

import com.unicorn.server.common.domain.Event
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.enums.NotificationEventType

class NotificationRequestedEvent(
	val channel: NotificationChannel,
	val receiver: String,
	val eventType: NotificationEventType,
	val payload: Map<String, String>,
	val dedupKey: String,
) : Event()
