package com.unicorn.server.domain.user.vo

@JvmInline
value class Email(val value: String) {
	init {
		if (value.isBlank()) {
			throw IllegalArgumentException("Email cannot be blank")
		}
		if (!EMAIL_PATTERN.matches(value.trim())) {
			throw IllegalArgumentException("Invalid email format: $value")
		}
	}

	override fun toString(): String = value

	companion object {
		private val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
	}
}
