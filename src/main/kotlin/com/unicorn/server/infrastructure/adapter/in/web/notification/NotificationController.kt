package com.unicorn.server.infrastructure.adapter.`in`.web.notification

import com.unicorn.server.domain.notification.port.`in`.NotificationInPort
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto.NotificationInboxItemResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto.NotificationInboxPageResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
	private val notificationInPort: NotificationInPort,
) : NotificationApiDoc {

	@GetMapping
	override fun getNotifications(
		@AuthenticationPrincipal memberId: String,
		@RequestParam(required = false) cursor: Long?,
		@RequestParam(defaultValue = "20") size: Int,
	): ApiResponse<NotificationInboxPageResponse> {
		val page = notificationInPort.getNotifications(memberId, cursor, size)
		return ApiResponse.success(NotificationInboxPageResponse.from(page))
	}

	@PatchMapping("/{notificationId}/read")
	override fun markNotificationAsRead(
		@AuthenticationPrincipal memberId: String,
		@PathVariable notificationId: Long,
	): ApiResponse<NotificationInboxItemResponse> {
		val item = notificationInPort.markNotificationAsRead(memberId, notificationId)
		return ApiResponse.success(NotificationInboxItemResponse.from(item))
	}
}
