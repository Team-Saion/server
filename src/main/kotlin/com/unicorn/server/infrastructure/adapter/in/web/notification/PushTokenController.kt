package com.unicorn.server.infrastructure.adapter.`in`.web.notification

import com.unicorn.server.domain.notification.port.`in`.NotificationPushTokenInPort
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto.PushTokenResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto.RegisterPushTokenRequest
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/push-tokens")
class PushTokenController(
	private val notificationPushTokenInPort: NotificationPushTokenInPort,
) : PushTokenApiDoc {

	@PostMapping
	override fun register(
		@AuthenticationPrincipal memberId: String,
		@RequestBody @Valid request: RegisterPushTokenRequest,
	): ApiResponse<PushTokenResponse> {
		val pushToken = notificationPushTokenInPort.register(memberId, request.toCommand())
		return ApiResponse.success(PushTokenResponse.from(pushToken))
	}

	@DeleteMapping("/{tokenId}")
	override fun deactivate(
		@AuthenticationPrincipal memberId: String,
		@PathVariable tokenId: Long,
	): ApiResponse<Unit> {
		notificationPushTokenInPort.deactivate(memberId, tokenId)
		return ApiResponse.success()
	}
}
