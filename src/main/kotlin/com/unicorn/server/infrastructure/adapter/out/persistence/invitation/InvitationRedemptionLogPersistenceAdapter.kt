package com.unicorn.server.infrastructure.adapter.out.persistence.invitation

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.invitation.InvitationRedemption
import com.unicorn.server.domain.invitation.port.out.InvitationRedemptionLogOutPort
import com.unicorn.server.infrastructure.adapter.out.persistence.invitation.entity.toDomain
import com.unicorn.server.infrastructure.adapter.out.persistence.invitation.entity.toEntity
import org.springframework.transaction.annotation.Transactional

@PersistenceAdapter
class InvitationRedemptionLogPersistenceAdapter(
	private val invitationRedemptionLogJpaRepository: InvitationRedemptionLogJpaRepository,
) : InvitationRedemptionLogOutPort {
	@Transactional
	override fun save(redemption: InvitationRedemption): InvitationRedemption =
		invitationRedemptionLogJpaRepository.save(redemption.toEntity()).toDomain()
}
