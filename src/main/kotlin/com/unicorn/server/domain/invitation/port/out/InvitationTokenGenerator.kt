package com.unicorn.server.domain.invitation.port.out

import com.unicorn.server.domain.invitation.vo.InvitationToken

interface InvitationTokenGenerator {
	fun generate(): InvitationToken
}
