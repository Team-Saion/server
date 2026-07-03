package com.unicorn.server.domain.invitation

import com.unicorn.server.domain.invitation.vo.InvitationId
import com.unicorn.server.domain.invitation.vo.InvitationRedemptionId
import com.unicorn.server.domain.member.vo.MemberId
import java.time.LocalDateTime

class InvitationRedemption internal constructor(
	val id: InvitationRedemptionId,
	val invitationId: InvitationId,
	val redeemerMemberId: MemberId,
	val redeemedAt: LocalDateTime,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
) {
	companion object {
		fun create(id: InvitationRedemptionId, invitationId: InvitationId, redeemerMemberId: MemberId, now: LocalDateTime): InvitationRedemption =
			InvitationRedemption(
				id = id,
				invitationId = invitationId,
				redeemerMemberId = redeemerMemberId,
				redeemedAt = now,
				createdAt = now,
				updatedAt = now,
			)
	}
}
