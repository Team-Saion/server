package com.unicorn.server.domain.invitation.vo

import java.util.UUID

@JvmInline
value class InvitationRedemptionId(val value: UUID) {
	override fun toString(): String = value.toString()

	companion object {
		fun generate(): InvitationRedemptionId = InvitationRedemptionId(UUID.randomUUID())
		fun of(value: String): InvitationRedemptionId = InvitationRedemptionId(UUID.fromString(value))
	}
}
