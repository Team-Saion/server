package com.unicorn.server.domain.invitation.port.out

import com.unicorn.server.domain.invitation.vo.InvitationId

interface InvitationIdGenerator {
	fun next(): InvitationId
}
