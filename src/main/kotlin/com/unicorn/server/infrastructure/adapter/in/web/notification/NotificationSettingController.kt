package com.unicorn.server.infrastructure.adapter.`in`.web.notification

import com.unicorn.server.domain.notification.port.`in`.NotificationSettingInPort
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto.NotificationSettingResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto.UpdateNotificationSettingRequest
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notification-settings")
class NotificationSettingController(
	private val notificationSettingInPort: NotificationSettingInPort,
) : NotificationSettingApiDoc {

	@GetMapping
	override fun getSetting(
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<NotificationSettingResponse> {
		val setting = notificationSettingInPort.getSetting(memberId)
		return ApiResponse.success(NotificationSettingResponse.from(setting))
	}

	@PutMapping
	override fun updateSetting(
		@AuthenticationPrincipal memberId: String,
		@RequestBody request: UpdateNotificationSettingRequest,
	): ApiResponse<NotificationSettingResponse> {
		val setting = notificationSettingInPort.updateSetting(memberId, request.toCommand())
		return ApiResponse.success(NotificationSettingResponse.from(setting))
	}
}
