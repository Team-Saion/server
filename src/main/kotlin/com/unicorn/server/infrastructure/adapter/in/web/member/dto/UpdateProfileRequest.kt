package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// UpdateProfileRequest - 프로필 변경 요청 DTO다.
data class UpdateProfileRequest(
	@field:NotBlank
	@field:Size(min = 2, max = 30)
	val nickname: String,
)
