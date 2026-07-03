package com.unicorn.server.domain.invitation.port.`in`

import com.unicorn.server.domain.invitation.port.dto.AcceptResult

interface AcceptCircleInvitationInPort {
	fun accept(token: String, memberId: String): AcceptResult
}
