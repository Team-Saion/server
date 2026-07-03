package com.unicorn.server.domain.invitation.vo

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@JvmInline
value class InvitationRedemptionId(val value: String) {
	override fun toString(): String = value

	companion object {
		private const val PREFIX = "IR"
		private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

		fun generate(sequence: Long): InvitationRedemptionId {
			val timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER)
			val paddedSequence = sequence.toString().padStart(5, '0')
			return InvitationRedemptionId("$PREFIX$timestamp$paddedSequence")
		}

		fun of(value: String): InvitationRedemptionId = InvitationRedemptionId(value)
	}
}
