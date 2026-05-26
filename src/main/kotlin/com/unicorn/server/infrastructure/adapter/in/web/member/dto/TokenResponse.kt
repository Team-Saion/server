package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import com.unicorn.server.domain.member.port.dto.TokenPair

// TokenResponse - 인증 토큰 발급 HTTP 응답 바디를 담는다.
data class TokenResponse(
	val accessToken: String,
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
