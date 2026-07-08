package com.unicorn.server.infrastructure.adapter.out.persistence.invitation

import com.unicorn.server.domain.invitation.enums.InvitationStatus
import com.unicorn.server.domain.invitation.enums.InvitationType
import com.unicorn.server.infrastructure.adapter.out.persistence.invitation.entity.InvitationEntity
import org.springframework.data.jpa.repository.JpaRepository

interface InvitationJpaRepository : JpaRepository<InvitationEntity, String> {
	fun findByToken(token: String): InvitationEntity?
	fun findAllByTypeAndTargetIdAndInviterIdAndStatusAndDelYn(
		type: InvitationType,
		targetId: String,
		inviterId: String,
		status: InvitationStatus,
		delYn: String,
	): List<InvitationEntity>
}
