package com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Schema(description = "초대장 발급 요청")
data class IssueInvitationRequest(
	@field:Schema(
		description = "초대 타입 (현재는 CIRCLE만 지원)",
		example = "CIRCLE",
		allowableValues = ["CIRCLE"],
	)
	@field:NotBlank
	@field:Pattern(regexp = "^(CIRCLE|SCHEDULE)$")
	val type: String,

	@field:Schema(
		description = "초대 대상 ID (써클 ID)",
		example = "CC20260101000000001",
	)
	@field:NotBlank
	@field:Size(max = 21)
	val targetId: String,

	@field:Schema(
		description = "초대받는 사람 이름 (선택)",
		example = "김철수",
	)
	@field:Size(max = 10)
	val inviteToName: String?,

	@field:Schema(
		description = "초대 메시지 (선택)",
		example = "함께 스터디해요!",
	)
	@field:Size(max = 50)
	val message: String?,
)
