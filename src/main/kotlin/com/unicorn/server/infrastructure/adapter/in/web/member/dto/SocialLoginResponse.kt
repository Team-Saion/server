package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import com.unicorn.server.domain.member.port.dto.SocialLoginResult
import io.swagger.v3.oas.annotations.media.Schema

// SocialLoginResponse - 소셜 로그인(카카오/애플 공통) HTTP 응답 바디를 담는다.
@Schema(description = "소셜 로그인 응답")
data class SocialLoginResponse(
	@field:Schema(
		description = "서비스 API 인증에 사용하는 access token",
		example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwMDEiLCJ0eXBlIjoiYWNjZXNzIiwicm9sZXMiOlsiTUVNQkVSIl19.signature",
	)
	val accessToken: String,

	@field:Schema(
		description = "access token 재발급에 사용하는 refresh token. 같은 계정으로 재로그인하면 이전 refresh token은 무효화된다.",
		example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwMDEiLCJ0eXBlIjoicmVmcmVzaCJ9.signature",
	)
	val refreshToken: String,

	@field:Schema(
		description = "true면 이번 로그인으로 신규 회원가입이 이루어진 경우, false면 기존 회원으로 로그인한 경우. 프론트에서 온보딩 플로우 분기에 사용한다.",
		example = "false",
	)
	val isNewMember: Boolean,
) {
	companion object {
		fun from(result: SocialLoginResult): SocialLoginResponse = SocialLoginResponse(
			accessToken = result.tokenPair.accessToken,
			refreshToken = result.tokenPair.refreshToken,
			isNewMember = result.isNewMember,
		)
	}
}
