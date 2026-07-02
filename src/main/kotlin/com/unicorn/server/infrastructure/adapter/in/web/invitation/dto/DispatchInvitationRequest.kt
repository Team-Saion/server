package com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "초대장 발송 채널 로깅 요청")
data class DispatchInvitationRequest(
	@field:Schema(
		description = "발송 채널 (LINK_COPY, MESSAGE, KAKAO)",
		example = "LINK_COPY",
		allowableValues = ["LINK_COPY", "MESSAGE", "KAKAO"],
	)
	val channel: String,
)
