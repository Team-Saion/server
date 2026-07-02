package com.unicorn.server.domain.invitation

import com.unicorn.server.domain.invitation.enums.InvitationStatus
import com.unicorn.server.domain.invitation.enums.InvitationType
import com.unicorn.server.domain.invitation.exception.InvitationSelfApprovalForbiddenException
import com.unicorn.server.domain.invitation.vo.InviteMessage
import com.unicorn.server.domain.invitation.vo.InviteToName
import com.unicorn.server.domain.invitation.vo.InvitationId
import com.unicorn.server.domain.invitation.vo.InvitationToken
import com.unicorn.server.domain.member.vo.MemberId
import java.time.LocalDateTime
import java.util.UUID

class Invitation internal constructor(
	val id: InvitationId,
	val type: InvitationType,
	val targetId: UUID,
	val token: InvitationToken,
	val inviterId: MemberId,
	val inviteToName: InviteToName?,
	val message: InviteMessage?,
	status: InvitationStatus,
	val expiresAt: LocalDateTime,
	deleted: Boolean,
	val createdAt: LocalDateTime,
	updatedAt: LocalDateTime,
) {
	var status: InvitationStatus = status
		private set

	var deleted: Boolean = deleted
		private set

	var updatedAt: LocalDateTime = updatedAt
		private set

	fun isExpired(now: LocalDateTime): Boolean = status == InvitationStatus.EXPIRED || now.isAfter(expiresAt)

	fun isUsable(now: LocalDateTime): Boolean = !deleted && !isExpired(now)

	fun ensureNotSelfApproval(redeemerMemberId: MemberId) {
		if (inviterId == redeemerMemberId) {
			throw InvitationSelfApprovalForbiddenException(id.toString())
		}
	}

	fun markExpired() {
		status = InvitationStatus.EXPIRED
		updatedAt = LocalDateTime.now()
	}

	companion object {
		private const val EXPIRE_HOURS = 48L

		fun create(
			type: InvitationType,
			targetId: UUID,
			token: InvitationToken,
			inviterId: MemberId,
			inviteToName: InviteToName?,
			message: InviteMessage?,
		): Invitation {
			val now = LocalDateTime.now()
			return Invitation(
				id = InvitationId.generate(),
				type = type,
				targetId = targetId,
				token = token,
				inviterId = inviterId,
				inviteToName = inviteToName,
				message = message,
				status = InvitationStatus.ACTIVE,
				expiresAt = now.plusHours(EXPIRE_HOURS),
				deleted = false,
				createdAt = now,
				updatedAt = now,
			)
		}
	}
}
