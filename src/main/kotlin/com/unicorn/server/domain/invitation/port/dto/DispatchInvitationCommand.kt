package com.unicorn.server.domain.invitation.port.dto

import com.unicorn.server.domain.invitation.enums.InvitationChannel

data class DispatchInvitationCommand(
	val invitationId: String,
	val channel: InvitationChannel,
)
