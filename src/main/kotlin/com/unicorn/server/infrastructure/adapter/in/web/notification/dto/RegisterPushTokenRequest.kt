package com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto

import com.unicorn.server.domain.notification.enums.DevicePlatform
import com.unicorn.server.domain.notification.port.dto.RegisterPushTokenCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "푸시 토큰 등록 요청")
data class RegisterPushTokenRequest(
	@field:NotBlank
	@field:Schema(description = "FCM 푸시 토큰", example = "fcm-token")
	val token: String,

	@field:Schema(description = "기기 플랫폼", example = "IOS")
	val platform: DevicePlatform,

	@field:Schema(description = "OS 알림 권한 허용 여부", example = "true")
	val osNotificationPermissionGranted: Boolean,

	@field:Schema(description = "앱 버전", nullable = true, example = "1.0.0")
	val appVersion: String?,
) {
	fun toCommand(): RegisterPushTokenCommand = RegisterPushTokenCommand(
		token = token,
		platform = platform,
		osNotificationPermissionGranted = osNotificationPermissionGranted,
		appVersion = appVersion,
	)
}
