package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import com.unicorn.server.domain.member.port.dto.TokenPair
import io.swagger.v3.oas.annotations.media.Schema

// TokenResponse - 인증 토큰 발급 HTTP 응답 바디를 담는다.
@Schema(description = "인증 토큰 응답")
data class TokenResponse(
	@field:Schema(
		description = "서비스 API 인증에 사용하는 access token",
		example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwMDEiLCJ0eXBlIjoiYWNjZXNzIiwicm9sZXMiOlsiTUVNQkVSIl19.signature",
	)
	val accessToken: String,

	@field:Schema(
		description = "access token 재발급에 사용하는 refresh token",
		example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwMDEiLCJ0eXBlIjoicmVmcmVzaCJ9.signature",
	)
	val refreshToken: String,
) {
	companion object {
		// 유스케이스 토큰 결과를 HTTP 응답 DTO로 변환한다.
		fun from(tokenPair: TokenPair): TokenResponse = TokenResponse(
			accessToken = tokenPair.accessToken,
			refreshToken = tokenPair.refreshToken,
		)
	}
}
