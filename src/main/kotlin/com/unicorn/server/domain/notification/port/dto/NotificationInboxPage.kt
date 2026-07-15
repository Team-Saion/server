package com.unicorn.server.domain.notification.port.dto

import com.unicorn.server.domain.notification.NotificationInboxItem

data class NotificationInboxPage(
	val items: List<NotificationInboxItem>,
	val nextCursor: Long?,
)
