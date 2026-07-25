package com.unicorn.server.domain.invitation.port.`in`

import com.unicorn.server.domain.invitation.port.dto.DispatchInvitationCommand

interface InvitationDispatchInPort {
	fun dispatch(command: DispatchInvitationCommand)
}
