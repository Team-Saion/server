package com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "초대장 발급 요청")
data class IssueInvitationRequest(
	@field:Schema(
		description = "초대 대상 ID (써클 ID)",
		example = "CC20260101000000001",
	)
	@field:NotBlank
	@field:Size(max = 21)
	val targetId: String,
)
