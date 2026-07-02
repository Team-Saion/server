package com.unicorn.server.domain.invitation.port.out

import com.unicorn.server.domain.invitation.enums.InvitationChannel
import com.unicorn.server.domain.invitation.vo.InvitationId
import java.time.LocalDateTime

interface InvitationDispatchLogOutPort {
	fun save(invitationId: InvitationId, channel: InvitationChannel, dispatchedAt: LocalDateTime)
}
