package com.unicorn.server.infrastructure.adapter.out.persistence.member.entity

import com.unicorn.server.common.persistence.AuditableJpaEntity
import com.unicorn.server.domain.member.SocialAccount
import com.unicorn.server.domain.member.enums.SocialProvider
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.domain.member.vo.SocialAccountId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

// SocialAccountEntity - SocialAccount 도메인을 JPA 테이블 row로 저장하고 복원한다.
@Entity
@Table(
	name = "social_account",
	uniqueConstraints = [
		UniqueConstraint(
			name = "uq_social_account_provider_provider_id",
			columnNames = ["provider", "provider_id"],
		),
	],
	indexes = [Index(name = "idx_social_account_member_id", columnList = "member_id")],
)
class SocialAccountEntity protected constructor() : AuditableJpaEntity() {

	@Id
	@Column(name = "id", nullable = false, length = 36)
	var id: String = ""
		protected set

	@Column(name = "member_id", nullable = false, length = 36)
	var memberId: String = ""
		protected set

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false, length = 20)
	lateinit var provider: SocialProvider
		protected set

	@Column(name = "provider_id", nullable = false, length = 255)
	var providerId: String = ""
		protected set

	@Column(name = "email", nullable = true, length = 255)
	var email: String? = null
		protected set

	constructor(socialAccount: SocialAccount) : this() {
		id = socialAccount.id.toString()
		memberId = socialAccount.memberId.toString()
		provider = socialAccount.provider
		providerId = socialAccount.providerId
		email = socialAccount.email
	}

	// 영속성 객체를 순수 도메인 객체로 복원한다.
	fun toDomain(): SocialAccount = SocialAccount.reconstitute(
		id = SocialAccountId.of(id),
		memberId = MemberId.of(memberId),
		provider = provider,
		providerId = providerId,
		email = email,
		createdAt = requireNotNull(createdAt) { "createdAt must not be null" },
	)
}
