package com.unicorn.server.common.vo

// Email 값 객체 - 서비스 전역에서 이메일 형식 검증과 값 동등성을 담당한다.
@JvmInline
value class Email(val value: String) {
	init {
		require(value.isNotBlank()) { "Email cannot be blank" }
		require(value == value.trim()) { "Email cannot contain leading or trailing whitespace" }
		require(EMAIL_PATTERN.matches(value)) { "Invalid email format: $value" }
	}

	// 이메일 값을 문자열로 노출한다.
	override fun toString(): String = value

	companion object {
		private val EMAIL_PATTERN = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
	}
}
