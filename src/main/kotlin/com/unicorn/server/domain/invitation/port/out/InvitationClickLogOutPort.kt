package com.unicorn.server.domain.invitation.port.out

import com.unicorn.server.domain.invitation.vo.InvitationId
import java.time.LocalDateTime

interface InvitationClickLogOutPort {
	fun save(invitationId: InvitationId, clickedAt: LocalDateTime)
}
