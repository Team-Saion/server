package com.unicorn.server.infrastructure.adapter.`in`.web.member.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class WithdrawRequest(
	@field:NotBlank
	@field:Size(max = 500)
	val reason: String,
)
