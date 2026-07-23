package com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto

import com.unicorn.server.domain.notification.enums.DevicePlatform
import com.unicorn.server.domain.notification.port.dto.RegisterPushTokenCommand
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "푸시 토큰 등록 요청")
data class RegisterPushTokenRequest(
	@field:NotBlank
	@field:Schema(description = "Firebase Installation ID", example = "firebase-installation-id")
	val installationId: String,

	@field:NotBlank
	@field:Schema(description = "FCM 푸시 토큰", example = "fcm-token")
	val token: String,

	@field:Schema(description = "기기 플랫폼", example = "IOS")
	val platform: DevicePlatform,
) {
	fun toCommand(): RegisterPushTokenCommand = RegisterPushTokenCommand(
		installationId = installationId,
		token = token,
		platform = platform,
	)
}
