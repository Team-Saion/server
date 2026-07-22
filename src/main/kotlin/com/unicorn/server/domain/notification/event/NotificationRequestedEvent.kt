package com.unicorn.server.domain.notification.event

import com.unicorn.server.common.domain.Event
import com.unicorn.server.domain.notification.enums.NotificationChannel

class NotificationRequestedEvent(
	val channel: NotificationChannel,
	val receiver: String,
	val payload: NotificationEventPayload,
	val dedupKey: String,
) : Event()
