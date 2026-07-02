package com.unicorn.server.infrastructure.adapter.`in`.web.circle

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto.CircleSummaryResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto.CreateCircleRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Circle API", description = "써클 생성 API")
interface CircleApiDoc {
	@Operation(
		summary = "써클 생성",
		description = """
			인증된 사용자가 새 써클을 생성합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- 써클 이름은 1자 이상 20자 이하입니다.
			- 써클 이름은 한글, 영문, 숫자만 허용합니다.
			- 생성자는 자동으로 INITIATOR 역할의 circle_member가 됩니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "CIRCLE_NAME_BLANK"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "CIRCLE_NAME_TOO_LONG"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "CIRCLE_NAME_INVALID_CHARSET"),
	)
	@ApiSuccessCodeExample(CircleSummaryResponse::class)
	fun create(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@RequestBody @Valid request: CreateCircleRequest,
	): ApiResponse<CircleSummaryResponse>
}
