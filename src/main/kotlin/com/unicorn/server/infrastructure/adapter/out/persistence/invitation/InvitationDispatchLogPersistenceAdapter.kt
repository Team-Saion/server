package com.unicorn.server.infrastructure.adapter.out.persistence.invitation

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.invitation.enums.InvitationChannel
import com.unicorn.server.domain.invitation.port.out.InvitationDispatchLogOutPort
import com.unicorn.server.domain.invitation.vo.InvitationId
import com.unicorn.server.infrastructure.adapter.out.persistence.invitation.entity.InvitationDispatchLogEntity
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@PersistenceAdapter
class InvitationDispatchLogPersistenceAdapter(
	private val invitationDispatchLogJpaRepository: InvitationDispatchLogJpaRepository,
) : InvitationDispatchLogOutPort {
	@Transactional
	override fun save(invitationId: InvitationId, channel: InvitationChannel, dispatchedAt: LocalDateTime) {
		invitationDispatchLogJpaRepository.save(InvitationDispatchLogEntity(invitationId.toString(), channel, dispatchedAt))
	}
}
