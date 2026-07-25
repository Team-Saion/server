package com.unicorn.server.domain.notification.port.dto

import com.unicorn.server.domain.notification.event.NotificationEventPayload

data class PublishNotificationCommand(
    val receiverMemberId: String,
    val payload: NotificationEventPayload,
    val eventId: String,
    val circleId: String? = null,
    val scheduleId: String? = null,
)
