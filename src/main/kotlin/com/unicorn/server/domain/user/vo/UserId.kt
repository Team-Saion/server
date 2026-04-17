package com.unicorn.server.domain.user.vo

import java.util.UUID

@JvmInline
value class UserId(val value: UUID) {
	override fun toString(): String = value.toString()

	companion object {
		fun generate(): UserId = UserId(UUID.randomUUID())
		fun of(value: String): UserId = UserId(UUID.fromString(value))
	}
}
