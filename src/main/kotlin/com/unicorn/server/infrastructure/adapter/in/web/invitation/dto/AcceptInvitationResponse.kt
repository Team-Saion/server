package com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto

import com.unicorn.server.domain.invitation.port.dto.AcceptResult

data class AcceptInvitationResponse(
	val circleId: String,
) {
	companion object {
		fun from(result: AcceptResult): AcceptInvitationResponse =
			AcceptInvitationResponse(result.circleId)
	}
}
