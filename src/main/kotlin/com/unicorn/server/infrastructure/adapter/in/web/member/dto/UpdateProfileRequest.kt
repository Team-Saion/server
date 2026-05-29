package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// UpdateProfileRequest - 프로필 변경 요청 DTO다.
@Schema(description = "프로필 변경 요청")
data class UpdateProfileRequest(
	@field:NotBlank
	@field:Size(min = 2, max = 30)
	@field:Schema(
		description = "변경할 닉네임. 2자 이상 30자 이하이며 앞뒤 공백은 허용하지 않는다.",
		example = "새닉네임",
		minLength = 2,
		maxLength = 30,
	)
	val nickname: String,
)
