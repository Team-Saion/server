package com.unicorn.server.domain.invitation.event

import com.unicorn.server.common.domain.Event
import com.unicorn.server.domain.invitation.enums.InvitationType

class InvitationIssuedEvent(
	val invitationId: String,
	val type: InvitationType,
	val targetId: String,
	val inviterId: String,
) : Event()
