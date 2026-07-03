package com.unicorn.server.domain.invitation.port.out

import com.unicorn.server.domain.invitation.InvitationRedemption

interface InvitationRedemptionLogOutPort {
	fun save(redemption: InvitationRedemption): InvitationRedemption
}
