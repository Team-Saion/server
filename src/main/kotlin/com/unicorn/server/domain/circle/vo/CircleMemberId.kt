package com.unicorn.server.domain.circle.vo

import java.util.UUID

@JvmInline
value class CircleMemberId(val value: UUID) {
	override fun toString(): String = value.toString()

	companion object {
		fun generate(): CircleMemberId = CircleMemberId(UUID.randomUUID())
		fun of(value: String): CircleMemberId = CircleMemberId(UUID.fromString(value))
	}
}
