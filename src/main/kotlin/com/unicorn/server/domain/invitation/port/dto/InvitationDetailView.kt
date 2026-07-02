package com.unicorn.server.domain.invitation.port.dto

import java.time.LocalDateTime

data class InvitationDetailView(
	val invitationId: String,
	val circleName: String,
	val inviterNickname: String,
	val inviterAvatarColor: String,
	val expiresAt: LocalDateTime,
)
