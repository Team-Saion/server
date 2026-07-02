package com.unicorn.server.infrastructure.adapter.`in`.web.term.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotEmpty

@Schema(description = "약관 동의 요청")
data class AgreeTermsRequest(
	@field:NotEmpty
	@field:Schema(
		description = "동의할 약관 ID 목록",
		example = "[1, 2, 3]",
	)
	val termIds: List<Long>,
)
