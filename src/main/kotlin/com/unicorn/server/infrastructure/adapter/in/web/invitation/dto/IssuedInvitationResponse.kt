package com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto

import com.unicorn.server.domain.invitation.port.dto.IssuedInvitationResult
import java.time.LocalDateTime

data class IssuedInvitationResponse(
	val invitationId: String,
	val token: String,
	val inviteUrl: String,
	val expiresAt: LocalDateTime,
) {
	companion object {
		fun from(result: IssuedInvitationResult): IssuedInvitationResponse =
			IssuedInvitationResponse(result.invitationId, result.token, result.inviteUrl, result.expiresAt)
	}
}
