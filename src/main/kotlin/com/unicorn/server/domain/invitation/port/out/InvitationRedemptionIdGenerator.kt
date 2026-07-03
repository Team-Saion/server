package com.unicorn.server.domain.invitation.port.out

import com.unicorn.server.domain.invitation.vo.InvitationRedemptionId

interface InvitationRedemptionIdGenerator {
	fun next(): InvitationRedemptionId
}
