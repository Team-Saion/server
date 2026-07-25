package com.unicorn.server.domain.invitation.port.`in`

import com.unicorn.server.domain.invitation.port.dto.AcceptResult

interface InvitationAcceptInPort {
	fun accept(token: String, memberId: String): AcceptResult
}
