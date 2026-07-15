package com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto

import com.unicorn.server.domain.notification.DevicePushToken
import com.unicorn.server.domain.notification.enums.DevicePlatform
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "푸시 토큰 응답")
data class PushTokenResponse(
	@field:Schema(description = "푸시 토큰 식별자", example = "1")
	val id: Long,

	@field:Schema(description = "기기 플랫폼", example = "IOS")
	val platform: DevicePlatform,

	@field:Schema(description = "OS 알림 권한 허용 여부", example = "true")
	val osNotificationPermissionGranted: Boolean,

	@field:Schema(description = "앱 버전", nullable = true, example = "1.0.0")
	val appVersion: String?,

	@field:Schema(description = "활성 여부", example = "true")
	val active: Boolean,
) {
	companion object {
		fun from(pushToken: DevicePushToken): PushTokenResponse = PushTokenResponse(
			id = requireNotNull(pushToken.id) { "Push token id must not be null" }.value,
			platform = pushToken.platform,
			osNotificationPermissionGranted = pushToken.osNotificationPermissionGranted,
			appVersion = pushToken.appVersion,
			active = pushToken.active,
		)
	}
}
