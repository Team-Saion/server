package com.unicorn.server.domain.invitation.event

import com.unicorn.server.common.domain.Event

class InvitationClickedEvent(
	val invitationId: String,
) : Event()
