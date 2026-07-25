package com.unicorn.server.domain.invitation.port.`in`

import com.unicorn.server.domain.invitation.port.dto.IssueInvitationCommand
import com.unicorn.server.domain.invitation.port.dto.IssuedInvitationResult

interface InvitationIssueInPort {
	fun issue(inviterMemberId: String, command: IssueInvitationCommand): IssuedInvitationResult
}
