package com.unicorn.server.domain.invitation.event

import com.unicorn.server.common.domain.Event
import com.unicorn.server.domain.invitation.enums.InvitationChannel

class InvitationDispatchedEvent(
	val invitationId: String,
	val channel: InvitationChannel,
) : Event()
