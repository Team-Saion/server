package com.unicorn.server.infrastructure.adapter.`in`.web.notification

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.notification.exception.NotificationErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto.NotificationInboxItemResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto.NotificationInboxPageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Notification API", description = "알림 보관함 조회 및 읽음 처리 API")
interface NotificationApiDoc {

	@Operation(
		summary = "알림 보관함 조회",
		description = """
			현재 인증된 멤버의 알림 보관함을 최신순으로 조회합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- 로그인 사용자 본인의 알림만 반환합니다.
			- cursor는 이전 응답의 nextCursor를 전달합니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
	)
	@ApiSuccessCodeExample(NotificationInboxPageResponse::class)
	fun getInbox(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@RequestParam(required = false) cursor: Long?,
		@RequestParam(defaultValue = "20") size: Int,
	): ApiResponse<NotificationInboxPageResponse>

	@Operation(
		summary = "알림 읽음 처리",
		description = """
			현재 인증된 멤버의 알림 보관함 항목을 읽음 처리합니다.

			- 이미 읽은 알림은 멱등하게 처리합니다.
			- MVP 화면에서는 읽음/안읽음 시각적 구분을 노출하지 않습니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = NotificationErrorCode::class, code = "NOTIFICATION_NOT_FOUND"),
	)
	@ApiSuccessCodeExample(NotificationInboxItemResponse::class)
	fun markRead(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@PathVariable notificationId: Long,
	): ApiResponse<NotificationInboxItemResponse>
}
