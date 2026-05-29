package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

// KakaoLoginRequest - 카카오 소셜 로그인 HTTP 요청 바디를 담는다.
@Schema(description = "카카오 소셜 로그인 요청")
data class KakaoLoginRequest(
	@field:NotBlank
	@field:Schema(
		description = "카카오에서 발급받은 ID Token",
		example = "eyJraWQiOiJrYWthby1rZXktaWQiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwiZW1haWwiOiJ1c2VyQGV4YW1wbGUuY29tIiwibmlja25hbWUiOiLtmZjsnYUiLCJhdWQiOiJrYWthby1hcHAta2V5IiwiaXNzIjoiaHR0cHM6Ly9rYXV0aC5rYWthby5jb20ifQ.signature",
	)
	val idToken: String,
)
