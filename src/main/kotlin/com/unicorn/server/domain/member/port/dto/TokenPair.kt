package com.unicorn.server.domain.member.port.dto

// TokenPair - 서비스 인증 토큰 쌍을 담는다.
data class TokenPair(
	val accessToken: String,
	val refreshToken: String,
)
