package com.unicorn.server.infrastructure.adapter.out.persistence.member

import com.unicorn.server.domain.member.enums.SocialProvider
import com.unicorn.server.infrastructure.adapter.out.persistence.member.entity.SocialAccountEntity
import org.springframework.data.jpa.repository.JpaRepository

// SocialAccountJpaRepository - social_account 테이블 접근을 담당하는 Spring Data JPA repository다.
interface SocialAccountJpaRepository : JpaRepository<SocialAccountEntity, String> {
	fun findByProviderAndProviderId(provider: SocialProvider, providerId: String): SocialAccountEntity?
	fun findByMemberId(memberId: String): SocialAccountEntity?
}
