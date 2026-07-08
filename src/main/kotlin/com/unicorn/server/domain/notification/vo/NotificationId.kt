package com.unicorn.server.domain.notification.vo

@JvmInline
value class NotificationId(val value: Long) {
	override fun toString(): String = value.toString()

	companion object {
		fun of(value: Long): NotificationId = NotificationId(value)
	}
}
