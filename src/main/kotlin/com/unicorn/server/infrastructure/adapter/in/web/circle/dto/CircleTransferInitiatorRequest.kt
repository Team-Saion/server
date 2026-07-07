package com.unicorn.server.infrastructure.adapter.`in`.web.circle.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

data class CircleTransferInitiatorRequest(
	@field:NotBlank
	@field:Schema(description = "권한을 위임받을 대상 멤버 ID", example = "11111111-1111-1111-1111-111111111111")
	val targetMemberId: String,
)
