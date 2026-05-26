package com.unicorn.server.domain.member.port.out

import com.unicorn.server.domain.member.SocialAccount
import com.unicorn.server.domain.member.enums.SocialProvider

// SocialAccountOutPort - 소셜 계정 저장소 기능을 정의한다.
interface SocialAccountOutPort {
	// 소셜 계정 연결 정보를 저장한다.
	fun save(socialAccount: SocialAccount): SocialAccount

	// 소셜 제공자와 제공자 회원 식별자로 연결 계정을 조회한다.
	fun findByProviderAndProviderId(provider: SocialProvider, providerId: String): SocialAccount?
}
