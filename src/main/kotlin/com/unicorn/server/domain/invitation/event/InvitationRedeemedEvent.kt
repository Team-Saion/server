package com.unicorn.server.domain.invitation.event

import com.unicorn.server.common.domain.Event
import com.unicorn.server.domain.invitation.enums.InvitationType

class InvitationRedeemedEvent(
	val invitationId: String,
	val type: InvitationType,
	val targetId: String,
	val inviterMemberId: String,
	val redeemerMemberId: String,
	val circleName: String,
	val redeemerNickname: String,
) : Event()
