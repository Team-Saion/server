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

	@field:Schema(description = "활성 여부", example = "true")
	val active: Boolean,
) {
	companion object {
		fun from(pushToken: DevicePushToken): PushTokenResponse = PushTokenResponse(
			id = requireNotNull(pushToken.id) { "Push token id must not be null" }.value,
			platform = pushToken.platform,
			active = pushToken.active,
		)
	}
}
