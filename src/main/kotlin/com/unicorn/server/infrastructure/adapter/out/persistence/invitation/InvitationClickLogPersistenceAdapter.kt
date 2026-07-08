package com.unicorn.server.infrastructure.adapter.out.persistence.invitation

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.invitation.port.out.InvitationClickLogOutPort
import com.unicorn.server.domain.invitation.vo.InvitationId
import com.unicorn.server.infrastructure.adapter.out.persistence.invitation.entity.InvitationClickLogEntity
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@PersistenceAdapter
class InvitationClickLogPersistenceAdapter(
	private val invitationClickLogJpaRepository: InvitationClickLogJpaRepository,
) : InvitationClickLogOutPort {
	@Transactional
	override fun save(invitationId: InvitationId, clickedAt: LocalDateTime) {
		invitationClickLogJpaRepository.save(InvitationClickLogEntity(invitationId.toString(), clickedAt))
	}
}
