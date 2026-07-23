package com.unicorn.server.domain.notification.port.dto

import com.unicorn.server.domain.notification.enums.DevicePlatform

data class RegisterPushTokenCommand(
	val installationId: String,
	val token: String,
	val platform: DevicePlatform,
)
