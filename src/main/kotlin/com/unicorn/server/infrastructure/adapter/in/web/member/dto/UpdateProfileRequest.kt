package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import io.swagger.v3.oas.annotations.media.Schema

// UpdateProfileRequest - 프로필 변경 요청 DTO다.
@Schema(description = "프로필 변경 요청")
data class UpdateProfileRequest(
	@field:Schema(
		description = "변경할 닉네임. 2자 이상 10자 이하이며 한글, 영문, 숫자만 허용하고 앞뒤 공백은 허용하지 않는다.",
		example = "새닉네임",
		minLength = 2,
		maxLength = 10,
	)
	val nickname: String,
)
