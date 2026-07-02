package com.unicorn.server.infrastructure.adapter.`in`.web.invitation.dto

data class IssueInvitationRequest(
	val type: String,
	val targetId: String,
	val inviteToName: String?,
	val message: String?,
)
