package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

// RefreshTokenRequest - 인증 토큰 재발급 HTTP 요청 바디를 담는다.
@Schema(description = "인증 토큰 재발급 요청")
data class RefreshTokenRequest(
	@field:NotBlank
	@field:Schema(
		description = "로그인 또는 직전 재발급에서 발급받은 refresh token",
		example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAwMDEiLCJ0eXBlIjoicmVmcmVzaCJ9.signature",
	)
	val refreshToken: String,
)
