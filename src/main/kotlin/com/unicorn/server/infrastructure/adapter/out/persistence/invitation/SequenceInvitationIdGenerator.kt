package com.unicorn.server.infrastructure.adapter.out.persistence.invitation

import com.unicorn.server.domain.invitation.port.out.InvitationIdGenerator
import com.unicorn.server.domain.invitation.vo.InvitationId
import com.unicorn.server.infrastructure.persistence.sequence.SequenceGenerator
import org.springframework.stereotype.Component

@Component
class SequenceInvitationIdGenerator(
	private val sequenceGenerator: SequenceGenerator,
) : InvitationIdGenerator {
	override fun next(): InvitationId = InvitationId.generate(sequenceGenerator.nextValue("invitation_seq"))
}
