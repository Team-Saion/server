package com.unicorn.server.domain.circle.vo

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@JvmInline
value class CircleMemberId(val value: String) {
	override fun toString(): String = value

	companion object {
		private const val PREFIX = "CM"
		private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

		fun generate(sequence: Long): CircleMemberId {
			val timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER)
			val paddedSequence = sequence.toString().padStart(5, '0')
			return CircleMemberId("$PREFIX$timestamp$paddedSequence")
		}

		fun of(value: String): CircleMemberId = CircleMemberId(value)
	}
}
