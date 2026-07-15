package com.unicorn.server.domain.notification.vo

@JvmInline
value class NotificationInboxItemId(val value: Long) {
	companion object {
		fun of(value: Long): NotificationInboxItemId = NotificationInboxItemId(value)
	}
}
