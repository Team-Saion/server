package com.unicorn.server.infrastructure.adapter.`in`.web.notification

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto.NotificationSettingResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto.UpdateNotificationSettingRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Notification Setting API", description = "알림 설정 조회 및 변경 API")
interface NotificationSettingApiDoc {

	@Operation(
		summary = "알림 설정 조회",
		description = "현재 인증된 멤버의 알림 설정을 조회합니다. 설정이 없으면 기본값 ON으로 생성합니다.",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
	)
	@ApiSuccessCodeExample(NotificationSettingResponse::class)
	fun getSetting(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<NotificationSettingResponse>

	@Operation(
		summary = "알림 설정 변경",
		description = "현재 인증된 멤버의 D-7, D-1, D-day, 가족 일정 확인 알림 설정을 변경합니다.",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
	)
	@ApiSuccessCodeExample(NotificationSettingResponse::class)
	fun updateSetting(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@RequestBody request: UpdateNotificationSettingRequest,
	): ApiResponse<NotificationSettingResponse>
}
