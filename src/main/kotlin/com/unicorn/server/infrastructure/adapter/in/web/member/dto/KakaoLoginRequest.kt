package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import jakarta.validation.constraints.NotBlank

// KakaoLoginRequest - 카카오 소셜 로그인 HTTP 요청 바디를 담는다.
data class KakaoLoginRequest(
	@field:NotBlank val idToken: String,
)
