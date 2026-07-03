package com.unicorn.server.domain.invitation.port.dto

import java.time.LocalDateTime

data class IssuedInvitationResult(
	val invitationId: String,
	val token: String,
	val expiresAt: LocalDateTime,
)
