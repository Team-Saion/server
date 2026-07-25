package com.unicorn.server.domain.notification.port.dto

import com.unicorn.server.domain.notification.event.NotificationEventPayload

data class RequestNotificationCommand(
	val receiverMemberId: String,
	val payload: NotificationEventPayload,
	val eventId: String,
	val circleId: String? = null,
	val scheduleId: String? = null,
)
