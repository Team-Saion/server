package com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto

import com.unicorn.server.domain.invitation.port.dto.InvitationDetailView
import java.time.LocalDateTime

data class InvitationDetailResponse(
	val invitationId: String,
	val circleName: String,
	val inviter: InviterResponse,
	val expiresAt: LocalDateTime,
) {
	data class InviterResponse(
		val nickname: String,
		val avatarColor: String,
	)

	companion object {
		fun from(view: InvitationDetailView): InvitationDetailResponse = InvitationDetailResponse(
			invitationId = view.invitationId,
			circleName = view.circleName,
			inviter = InviterResponse(view.inviterNickname, view.inviterAvatarColor),
			expiresAt = view.expiresAt,
		)
	}
}
