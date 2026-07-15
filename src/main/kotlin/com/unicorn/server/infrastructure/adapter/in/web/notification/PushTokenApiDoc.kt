package com.unicorn.server.infrastructure.adapter.`in`.web.notification

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.notification.exception.NotificationErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto.PushTokenResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto.RegisterPushTokenRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Push Token API", description = "푸시 토큰 등록 및 비활성화 API")
interface PushTokenApiDoc {

	@Operation(
		summary = "푸시 토큰 등록",
		description = "로그인, 앱 실행, 토큰 변경 시 최신 푸시 토큰과 OS 권한 상태를 저장합니다.",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
	)
	@ApiSuccessCodeExample(PushTokenResponse::class)
	fun register(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@RequestBody @Valid request: RegisterPushTokenRequest,
	): ApiResponse<PushTokenResponse>

	@Operation(
		summary = "푸시 토큰 비활성화",
		description = "현재 인증된 멤버의 푸시 토큰을 비활성화합니다.",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = NotificationErrorCode::class, code = "NOTIFICATION_NOT_FOUND"),
	)
	@ApiSuccessCodeExample(Unit::class)
	fun deactivate(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@PathVariable tokenId: Long,
	): ApiResponse<Unit>
}
