package com.unicorn.server.domain.invitation.port.out

import com.unicorn.server.domain.invitation.Invitation
import com.unicorn.server.domain.invitation.enums.InvitationType
import com.unicorn.server.domain.invitation.vo.InvitationId
import com.unicorn.server.domain.invitation.vo.InvitationToken
import com.unicorn.server.domain.member.vo.MemberId

interface InvitationOutPort {
	fun save(invitation: Invitation): Invitation
	fun findById(invitationId: InvitationId): Invitation?
	fun findByToken(token: InvitationToken): Invitation?
	fun findAllActiveByTypeAndTargetIdAndInviterId(type: InvitationType, targetId: String, inviterId: MemberId): List<Invitation>
}
