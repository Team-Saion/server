package com.unicorn.server.infrastructure.adapter.out.persistence.invitation.entity

import com.unicorn.server.common.persistence.AuditableJpaEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "invitation_redemption_log")
class InvitationRedemptionLogEntity internal constructor() : AuditableJpaEntity() {
	@Id
	@Column(name = "id", nullable = false, length = 21)
	var id: String = ""
		internal set

	@Column(name = "invitation_id", nullable = false, length = 21)
	var invitationId: String = ""
		internal set

	@Column(name = "redeemer_member_id", nullable = false, length = 36)
	var redeemerMemberId: String = ""
		internal set

	@Column(name = "redeemed_at", nullable = false)
	lateinit var redeemedAt: java.time.LocalDateTime
		internal set


}
