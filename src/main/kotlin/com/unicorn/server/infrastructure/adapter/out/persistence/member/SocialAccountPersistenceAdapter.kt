package com.unicorn.server.infrastructure.adapter.out.persistence.member

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.member.SocialAccount
import com.unicorn.server.domain.member.enums.SocialProvider
import com.unicorn.server.domain.member.port.out.SocialAccountOutPort
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.infrastructure.adapter.out.persistence.member.entity.toDomain
import com.unicorn.server.infrastructure.adapter.out.persistence.member.entity.toEntity
import org.springframework.transaction.annotation.Transactional

// SocialAccountPersistenceAdapter - SocialAccountOutPort를 JPA 저장소로 구현한다.
@PersistenceAdapter
class SocialAccountPersistenceAdapter(
	private val socialAccountJpaRepository: SocialAccountJpaRepository,
) : SocialAccountOutPort {

	// 소셜 계정을 신규 저장하거나 기존 row를 반환한다.
	@Transactional
	override fun save(socialAccount: SocialAccount): SocialAccount {
		// 기존 entity 조회 또는 신규 entity 조립
		val entity = socialAccountJpaRepository.findById(socialAccount.id.toString())
			.orElseGet { socialAccount.toEntity() }

		// 저장 후 도메인으로 복원
		return socialAccountJpaRepository.save(entity).toDomain()
	}

	// 소셜 제공자와 제공자 회원 식별자로 소셜 계정을 조회한다.
	@Transactional(readOnly = true)
	override fun findByProviderAndProviderId(provider: SocialProvider, providerId: String): SocialAccount? =
		socialAccountJpaRepository.findByProviderAndProviderId(provider, providerId)?.toDomain()

	// 멤버 식별자로 소셜 계정을 조회한다.
	@Transactional(readOnly = true)
	override fun findByMemberId(memberId: MemberId): SocialAccount? =
		socialAccountJpaRepository.findByMemberId(memberId.toString())?.toDomain()
}
