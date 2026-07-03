package com.unicorn.server.domain.invitation.port.dto

import com.unicorn.server.domain.invitation.enums.InvitationType

data class IssueInvitationCommand(
	val type: InvitationType,
	val targetId: String,
	val inviteToName: String?,
	val message: String?,
)
