package com.unicorn.server.domain.member.vo

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// MemberId 값 객체 - 멤버 식별자 생성과 문자열 변환을 담당한다.
@JvmInline
value class MemberId(val value: String) {
	// String 값을 그대로 노출한다.
	override fun toString(): String = value

	companion object {
		private const val PREFIX = "MB"
		private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

		// 신규 멤버 식별자를 생성한다.
		fun generate(sequence: Long): MemberId {
			val timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER)
			val paddedSequence = sequence.toString().padStart(5, '0')
			return MemberId("$PREFIX$timestamp$paddedSequence")
		}

		// 문자열을 멤버 식별자로 복원한다.
		fun of(value: String): MemberId = MemberId(value)
	}
}
