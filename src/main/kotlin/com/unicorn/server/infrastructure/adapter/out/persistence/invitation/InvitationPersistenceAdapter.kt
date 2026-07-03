package com.unicorn.server.infrastructure.adapter.out.persistence.invitation

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.invitation.Invitation
import com.unicorn.server.domain.invitation.port.out.InvitationOutPort
import com.unicorn.server.domain.invitation.vo.InvitationId
import com.unicorn.server.domain.invitation.vo.InvitationToken
import com.unicorn.server.infrastructure.adapter.out.persistence.invitation.entity.toDomain
import com.unicorn.server.infrastructure.adapter.out.persistence.invitation.entity.toEntity
import org.springframework.transaction.annotation.Transactional

@PersistenceAdapter
class InvitationPersistenceAdapter(
	private val invitationJpaRepository: InvitationJpaRepository,
) : InvitationOutPort {
	@Transactional
	override fun save(invitation: Invitation): Invitation {
		val entity = invitation.toEntity()
		return invitationJpaRepository.save(entity).toDomain()
	}

	@Transactional(readOnly = true)
	override fun findById(invitationId: InvitationId): Invitation? =
		invitationJpaRepository.findById(invitationId.toString()).map { it.toDomain() }.orElse(null)

	@Transactional(readOnly = true)
	override fun findByToken(token: InvitationToken): Invitation? =
		invitationJpaRepository.findByToken(token.value)?.toDomain()
}
