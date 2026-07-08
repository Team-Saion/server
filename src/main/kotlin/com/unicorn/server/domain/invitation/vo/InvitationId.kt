package com.unicorn.server.domain.invitation.vo

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@JvmInline
value class InvitationId(val value: String) {
	override fun toString(): String = value

	companion object {
		private const val PREFIX = "IV"
		private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

		fun generate(sequence: Long): InvitationId {
			val timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER)
			val paddedSequence = sequence.toString().padStart(5, '0')
			return InvitationId("$PREFIX$timestamp$paddedSequence")
		}

		fun of(value: String): InvitationId = InvitationId(value)
	}
}
