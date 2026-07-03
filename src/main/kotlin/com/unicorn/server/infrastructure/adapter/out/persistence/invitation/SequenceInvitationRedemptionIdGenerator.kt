package com.unicorn.server.infrastructure.adapter.out.persistence.invitation

import com.unicorn.server.domain.invitation.port.out.InvitationRedemptionIdGenerator
import com.unicorn.server.domain.invitation.vo.InvitationRedemptionId
import com.unicorn.server.infrastructure.persistence.sequence.SequenceGenerator
import org.springframework.stereotype.Component

@Component
class SequenceInvitationRedemptionIdGenerator(
	private val sequenceGenerator: SequenceGenerator,
) : InvitationRedemptionIdGenerator {
	override fun next(): InvitationRedemptionId = InvitationRedemptionId.generate(sequenceGenerator.nextValue("invitation_redemption_seq"))
}
