package com.unicorn.server.domain.member.vo

import java.util.UUID

// SocialAccountId 값 객체 - 소셜 계정 식별자 생성과 문자열 변환을 담당한다.
@JvmInline
value class SocialAccountId(val value: UUID) {
	// UUID 값을 문자열로 노출한다.
	override fun toString(): String = value.toString()

	companion object {
		// 신규 소셜 계정 식별자를 생성한다.
		fun generate(): SocialAccountId = SocialAccountId(UUID.randomUUID())

		// 문자열 UUID를 소셜 계정 식별자로 복원한다.
		fun of(value: String): SocialAccountId = SocialAccountId(UUID.fromString(value))
	}
}
