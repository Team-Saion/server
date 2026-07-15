package com.unicorn.server.domain.notification.vo

@JvmInline
value class DevicePushTokenId(val value: Long) {
	companion object {
		fun of(value: Long): DevicePushTokenId = DevicePushTokenId(value)
	}
}
