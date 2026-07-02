package com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "초대장 발급 요청")
data class IssueInvitationRequest(
	@field:Schema(
		description = "초대 타입 (현재는 CIRCLE만 지원)",
		example = "CIRCLE",
		allowableValues = ["CIRCLE"],
	)
	val type: String,

	@field:Schema(
		description = "초대 대상 ID (써클 ID)",
		example = "550e8400-e29b-41d4-a716-446655440000",
	)
	val targetId: String,

	@field:Schema(
		description = "초대받는 사람 이름 (선택)",
		example = "김철수",
	)
	val inviteToName: String?,

	@field:Schema(
		description = "초대 메시지 (선택)",
		example = "함께 스터디해요!",
	)
	val message: String?,
)
