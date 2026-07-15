package com.unicorn.server.domain.notification.port.dto

import com.unicorn.server.domain.notification.enums.DevicePlatform

data class RegisterPushTokenCommand(
	val token: String,
	val platform: DevicePlatform,
	val osNotificationPermissionGranted: Boolean,
	val appVersion: String?,
)
