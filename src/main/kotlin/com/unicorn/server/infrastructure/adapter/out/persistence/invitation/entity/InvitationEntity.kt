package com.unicorn.server.infrastructure.adapter.out.persistence.invitation.entity

import com.unicorn.server.common.persistence.AuditableJpaEntity
import com.unicorn.server.domain.invitation.enums.InvitationStatus
import com.unicorn.server.domain.invitation.enums.InvitationType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
	name = "invitation",
	indexes = [
		Index(name = "idx_invitation_type_target_id", columnList = "type,target_id"),
		Index(name = "idx_invitation_inviter_id", columnList = "inviter_id"),
		Index(name = "idx_invitation_status_expires_at", columnList = "status,expires_at"),
	],
)
class InvitationEntity internal constructor() : AuditableJpaEntity() {
	@Id
	@Column(name = "id", nullable = false, length = 21)
	var id: String = ""
		internal set

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 20)
	var type: InvitationType = InvitationType.CIRCLE
		internal set

	@Column(name = "target_id", nullable = false, length = 21)
	var targetId: String = ""
		internal set

	@Column(name = "token", nullable = false, length = 64, unique = true)
	var token: String = ""
		internal set

	@Column(name = "inviter_id", nullable = false, length = 36)
	var inviterId: String = ""
		internal set

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	var status: InvitationStatus = InvitationStatus.ACTIVE
		internal set

	@Column(name = "expires_at", nullable = false)
	var expiresAt: LocalDateTime = LocalDateTime.MIN
		internal set

	@Column(name = "del_yn", nullable = false, length = 1)
	var delYn: String = "N"
		internal set


}
