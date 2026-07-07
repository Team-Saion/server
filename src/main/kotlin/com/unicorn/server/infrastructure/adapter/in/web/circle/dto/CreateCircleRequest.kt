package com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "써클 생성 요청")
data class CreateCircleRequest(
	@field:Schema(
		description = "써클 이름",
		example = "유니콘 스터디",
	)
	@field:NotBlank
	@field:Size(max = 20)
	val name: String,
)
