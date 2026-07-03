package com.unicorn.server.infrastructure.adapter.out.persistence.member.entity

import com.unicorn.server.common.persistence.AuditableJpaEntity
import com.unicorn.server.domain.member.enums.SocialProvider
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
class SocialAccountEntity internal constructor() : AuditableJpaEntity() {

	@Id
	@Column(name = "id", nullable = false, length = 21)
	var id: String = ""
		internal set

	@Column(name = "member_id", nullable = false, length = 21)
	var memberId: String = ""
		internal set

	@Enumerated(EnumType.STRING)
	@Column(name = "provider", nullable = false, length = 20)
	lateinit var provider: SocialProvider
		internal set

	@Column(name = "provider_id", nullable = false, length = 255)
	var providerId: String = ""
		internal set

	@Column(name = "email", nullable = true, length = 255)
	var email: String? = null
		internal set

	@Column(name = "kakao_nickname", nullable = true, length = 100)
	var kakaoNickname: String? = null
		internal set

	@Column(name = "kakao_profile_image_url", nullable = true, length = 512)
	var kakaoProfileImageUrl: String? = null
		internal set


}
