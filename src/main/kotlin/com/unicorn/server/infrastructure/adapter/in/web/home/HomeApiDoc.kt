package com.unicorn.server.infrastructure.adapter.`in`.web.home

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto.CircleHomeResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto.CircleMemberResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Home API", description = "써클 홈 및 홈 구성원 조회 API")
interface HomeApiDoc {
	@Operation(
		summary = "써클 홈 조회",
			description = """
			써클 홈 화면에 필요한 정보를 조회합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- 요청자가 해당 써클의 활성 구성원이 아니면 403을 반환합니다.
			- 회원 탈퇴한 구성원은 응답에서 자동 제외됩니다.
			- 구성원 정보에는 프로필 이미지 URL(`profileImageUrl`)과 아바타 색상(`avatarColor.code`, `avatarColor.hex`)이 포함됩니다.
			- `mainSchedule`은 오늘 이후 가장 임박한 일정 1건이며, 예정된 일정이 없으면 null입니다.
			- `schedules`는 `mainSchedule`을 제외한 이후 일정들을 임박한 순서로 담습니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "FORBIDDEN"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "CIRCLE_NOT_FOUND"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
	)
	@ApiSuccessCodeExample(CircleHomeResponse::class)
	fun getHome(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
	): ApiResponse<CircleHomeResponse>

	@Operation(
		summary = "써클 구성원 조회",
			description = """
			써클의 현재 노출 가능한 구성원 목록을 조회합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- 회원 탈퇴한 구성원은 응답에서 자동 제외됩니다.
			- 각 구성원은 프로필 이미지 URL(`profileImageUrl`)과 아바타 색상(`avatarColor.code`, `avatarColor.hex`)을 포함합니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "FORBIDDEN"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "CIRCLE_NOT_FOUND"),
		ApiErrorCodeExample(codeType = CircleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
	)
	@ApiSuccessCodeExample(CircleMemberResponse::class)
	fun getMembers(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
	): ApiResponse<List<CircleMemberResponse>>
}
