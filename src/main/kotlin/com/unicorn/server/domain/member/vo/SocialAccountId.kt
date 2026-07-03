package com.unicorn.server.domain.member.vo

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// SocialAccountId 값 객체 - 소셜 계정 식별자 생성과 문자열 변환을 담당한다.
@JvmInline
value class SocialAccountId(val value: String) {
	override fun toString(): String = value

	companion object {
		private const val PREFIX = "SA"
		private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

		fun generate(sequence: Long): SocialAccountId {
			val timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER)
			val paddedSequence = sequence.toString().padStart(5, '0')
			return SocialAccountId("$PREFIX$timestamp$paddedSequence")
		}

		fun of(value: String): SocialAccountId = SocialAccountId(value)
	}
}
