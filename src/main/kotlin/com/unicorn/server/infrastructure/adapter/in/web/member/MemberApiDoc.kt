package com.unicorn.server.infrastructure.adapter.`in`.web.member

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.member.exception.MemberErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.MemberResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.UpdateProfileRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Member API", description = "인증된 멤버의 프로필, 로그아웃, 회원탈퇴 API")
interface MemberApiDoc {

	@Operation(
		summary = "내 프로필 조회",
		description = """
			현재 인증된 멤버의 프로필 정보를 조회합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- 탈퇴한 멤버는 접근할 수 없습니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "MEMBER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "WITHDRAWN_MEMBER"),
	)
	@ApiSuccessCodeExample(MemberResponse::class)
	fun getMyProfile(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<MemberResponse>

	@Operation(
		summary = "내 프로필 수정",
		description = """
			현재 인증된 멤버의 닉네임을 변경합니다.

			- 요청 바디: `nickname`
			- 닉네임은 2자 이상 30자 이하입니다.
			- 앞뒤 공백이 포함된 닉네임은 허용하지 않습니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "MEMBER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "WITHDRAWN_MEMBER"),
	)
	@ApiSuccessCodeExample(MemberResponse::class)
	fun updateProfile(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@RequestBody @Valid request: UpdateProfileRequest,
	): ApiResponse<MemberResponse>

	@Operation(
		summary = "로그아웃",
		description = """
			현재 인증된 멤버의 refresh token을 무효화합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- access token 자체는 stateless JWT이므로 서버 저장소에서 삭제하지 않습니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "MEMBER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "WITHDRAWN_MEMBER"),
	)
	@ApiSuccessCodeExample(Unit::class)
	fun logout(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<Unit>

	@Operation(
		summary = "회원 탈퇴",
		description = """
			현재 인증된 멤버를 소프트 삭제 처리합니다.

			- 복구 정책과 보관 기간을 고려해 즉시 물리 삭제하지 않습니다.
			- 탈퇴 시 refresh token도 무효화합니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "MEMBER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "WITHDRAWN_MEMBER"),
	)
	@ApiSuccessCodeExample(Unit::class)
	fun withdraw(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
	): ApiResponse<Unit>
}
