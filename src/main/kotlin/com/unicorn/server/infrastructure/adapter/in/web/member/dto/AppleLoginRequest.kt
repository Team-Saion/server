package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

// AppleLoginRequest - 애플 소셜 로그인 HTTP 요청 바디를 담는다.
@Schema(description = "애플 소셜 로그인 요청")
data class AppleLoginRequest(
	@field:NotBlank
	@field:Schema(
		description = "애플에서 발급받은 ID Token",
		example = "eyJraWQiOiJhcHBsZS1rZXktaWQiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIwMDEyMzQuYWJjZGVmMTIzNDU2LmFwcGxlLmNvbSIsImVtYWlsIjoidXNlckBwcml2YXRlcmVsYXkuYXBwbGVpZC5jb20iLCJhdWQiOiJjb20udW5pY29ybi5zYWlvbiIsImlzcyI6Imh0dHBzOi8vYXBwbGVpZC5hcHBsZS5jb20ifQ.signature",
	)
	val idToken: String,
)
