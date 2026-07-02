package com.unicorn.server.domain.invitation.port.`in`

import com.unicorn.server.domain.invitation.port.dto.InvitationDetailView

interface GetInvitationByTokenInPort {
	fun getByToken(token: String): InvitationDetailView
}
