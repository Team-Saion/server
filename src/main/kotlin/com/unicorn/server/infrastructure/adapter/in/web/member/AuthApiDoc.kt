package com.unicorn.server.infrastructure.adapter.`in`.web.member

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.member.exception.MemberErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.KakaoLoginRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.member.dto.KakaoLoginResponse
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
			- `isNewMember`가 `true`이면 해당 소셜 계정이 처음 가입한 것입니다.
			- 온보딩 진입 여부는 `isNewMember`가 아닌 응답의 `role` 값을 기준으로 판단하세요.
			  - `role = PENDING`: 온보딩 미완료 → 약관 동의 → 닉네임 설정 화면으로 진입
			  - `role = MEMBER`: 온보딩 완료 → 서비스 메인 화면으로 진입
			- 기존 회원이 재로그인하면 `isNewMember = false`이지만 `role = PENDING`일 수 있습니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "INVALID_SOCIAL_TOKEN"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "DUPLICATE_EMAIL"),
		ApiErrorCodeExample(codeType = MemberErrorCode::class, code = "WITHDRAWN_MEMBER"),
	)
	@ApiSuccessCodeExample(KakaoLoginResponse::class)
	fun kakaoLogin(
		@RequestBody @Valid request: KakaoLoginRequest,
	): ApiResponse<KakaoLoginResponse>

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
