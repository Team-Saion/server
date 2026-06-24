package com.unicorn.server.infrastructure.adapter.`in`.web.member

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.member.exception.MemberErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.KakaoLoginRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.RefreshTokenRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.TokenResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Auth API", description = "소셜 로그인 및 인증 토큰 관련 API")
interface AuthApiDoc {

	@Operation(
		summary = "카카오 소셜 로그인",
		description = """
			카카오 ID Token을 검증하고 서비스 access token, refresh token을 발급합니다.

			- 요청 바디: `idToken`
			- ID Token은 카카오 SDK 또는 카카오 로그인 플로우에서 발급받은 토큰입니다.
			- 서버는 카카오 JWKS로 서명, 만료, issuer, audience를 검증합니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "INVALID_SOCIAL_TOKEN"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "DUPLICATE_EMAIL"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "WITHDRAWN_MEMBER"),
	)
	@ApiSuccessCodeExample(TokenResponse::class)
	fun kakaoLogin(
		@RequestBody @Valid request: KakaoLoginRequest,
	): ApiResponse<TokenResponse>

	@Operation(
		summary = "인증 토큰 재발급",
		description = """
			유효한 refresh token을 검증하고 새로운 access token, refresh token을 발급합니다.

			- 요청 바디: `refreshToken`
			- 재발급이 완료되면 기존 refresh token은 즉시 무효화됩니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "INVALID_REFRESH_TOKEN"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "MEMBER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "WITHDRAWN_MEMBER"),
	)
	@ApiSuccessCodeExample(TokenResponse::class)
	fun reissue(
		@RequestBody @Valid request: RefreshTokenRequest,
	): ApiResponse<TokenResponse>
}
