package com.unicorn.server.domain.circle.vo

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@JvmInline
value class CircleId(val value: String) {
	override fun toString(): String = value

	companion object {
		private const val PREFIX = "CC"
		private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

		fun generate(sequence: Long): CircleId {
			val timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER)
			val paddedSequence = sequence.toString().padStart(5, '0')
			return CircleId("$PREFIX$timestamp$paddedSequence")
		}

		fun of(value: String): CircleId = CircleId(value)
	}
}
