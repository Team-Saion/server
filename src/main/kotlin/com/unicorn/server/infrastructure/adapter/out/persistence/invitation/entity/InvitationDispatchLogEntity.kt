package com.unicorn.server.infrastructure.adapter.out.persistence.invitation.entity

import com.unicorn.server.common.persistence.AuditableJpaEntity
import com.unicorn.server.domain.invitation.enums.InvitationChannel
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "invitation_dispatch_log")
class InvitationDispatchLogEntity protected constructor() : AuditableJpaEntity() {
	@Id
	@Column(name = "id", nullable = false, length = 36)
	var id: String = ""
		protected set

	@Column(name = "invitation_id", nullable = false, length = 36)
	var invitationId: String = ""
		protected set

	@Enumerated(EnumType.STRING)
	@Column(name = "channel", nullable = false, length = 20)
	lateinit var channel: InvitationChannel
		protected set

	@Column(name = "dispatched_at", nullable = false)
	lateinit var dispatchedAt: LocalDateTime
		protected set

	constructor(invitationId: String, channel: InvitationChannel, dispatchedAt: LocalDateTime) : this() {
		id = UUID.randomUUID().toString()
		this.invitationId = invitationId
		this.channel = channel
		this.dispatchedAt = dispatchedAt
		createdAt = dispatchedAt
		updatedAt = dispatchedAt
	}
}
