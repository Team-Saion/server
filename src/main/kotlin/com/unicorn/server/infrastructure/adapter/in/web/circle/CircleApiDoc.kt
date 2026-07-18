package com.unicorn.server.infrastructure.adapter.`in`.web.circle

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto.CircleSummaryResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto.CreateCircleRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto.CircleTransferInitiatorRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Circle API", description = "써클 생성 및 목록 조회 API")
interface CircleApiDoc {
	@Operation(
		summary = "내 써클 목록 조회",
		description = """
			인증된 사용자가 속한 모든 써클 목록을 조회합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- 생성한 써클뿐 아니라 현재 활성 구성원으로 속한 써클을 모두 반환합니다.
			- soft delete 된 써클은 응답에서 제외됩니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
	)
	@ApiSuccessCodeExample(CircleSummaryResponse::class)
	fun listCircles(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<List<CircleSummaryResponse>>

	@Operation(
		summary = "써클 생성",
			description = """
			인증된 사용자가 새 써클을 생성합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- 써클 이름은 1자 이상 20자 이하입니다.
			- 써클 이름은 특수문자와 이모지를 허용하되, 보안상 위험한 문자는 제한됩니다.
			- 생성자는 자동으로 INITIATOR 역할의 circle_member가 됩니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "CIRCLE_NAME_BLANK"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "CIRCLE_NAME_TOO_LONG"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "CIRCLE_NAME_INVALID_CHARSET"),
	)
	@SwaggerApiResponse(responseCode = "201", description = "Created")
	@ApiSuccessCodeExample(CircleSummaryResponse::class)
	fun create(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@RequestBody @Valid request: CreateCircleRequest,
	): ApiResponse<CircleSummaryResponse>

	@Operation(
		summary = "써클 탈퇴",
		description = """
			인증된 사용자가 현재 참여 중인 써클에서 탈퇴합니다.

			- 멤버십은 물리적으로 삭제하지 않고 LEFT 상태와 soft delete로 변경합니다.
			- 방장이 탈퇴하면 가입일이 가장 오래된 활성 구성원에게 권한이 자동 위임됩니다.
			- 방장이 유일한 구성원이라면 써클과 멤버십이 함께 soft delete 됩니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "CIRCLE_NOT_FOUND"),
	)
	fun leave(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "써클 ID")
		circleId: String,
	): ApiResponse<Unit>

	@Operation(
		summary = "써클 방장 권한 위임",
		description = """
			현재 INITIATOR가 같은 써클의 다른 활성 구성원에게 방장 권한을 위임합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- 현재 방장만 위임할 수 있습니다.
			- 자기 자신에게는 위임할 수 없습니다.
			- 같은 써클의 다른 활성 구성원에게만 위임할 수 있습니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "INITIATOR_DELEGATION_SELF_FORBIDDEN"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "INITIATOR_DELEGATION_TARGET_INVALID"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "INITIATOR_DELEGATION_FORBIDDEN"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "CIRCLE_NOT_FOUND"),
	)
	@ApiSuccessCodeExample(CircleSummaryResponse::class)
	fun transferInitiator(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@Parameter(description = "써클 ID")
		circleId: String,
		@RequestBody @Valid request: CircleTransferInitiatorRequest,
	): ApiResponse<CircleSummaryResponse>
}
