package com.unicorn.server.domain.invitation.vo

import java.util.UUID

@JvmInline
value class InvitationId(val value: UUID) {
	override fun toString(): String = value.toString()

	companion object {
		fun generate(): InvitationId = InvitationId(UUID.randomUUID())
		fun of(value: String): InvitationId = InvitationId(UUID.fromString(value))
	}
}
