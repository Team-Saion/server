package com.unicorn.server.domain.circle.vo

import java.util.UUID

@JvmInline
value class CircleId(val value: UUID) {
	override fun toString(): String = value.toString()

	companion object {
		fun generate(): CircleId = CircleId(UUID.randomUUID())
		fun of(value: String): CircleId = CircleId(UUID.fromString(value))
	}
}
