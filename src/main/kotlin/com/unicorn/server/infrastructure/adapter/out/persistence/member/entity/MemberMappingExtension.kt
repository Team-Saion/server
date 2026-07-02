package com.unicorn.server.infrastructure.adapter.out.persistence.member.entity

import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.SocialAccount
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.domain.member.vo.SocialAccountId


// ========== MemberEntity ↔ Member ==========

/**
 * Member 도메인 객체를 MemberEntity로 변환한다.
 */
fun Member.toEntity(): MemberEntity = MemberEntity().apply {
	id = this@toEntity.id.toString()
	email = this@toEntity.email?.value
	name = this@toEntity.name
	nickname = this@toEntity.nickname
	avatarColor = this@toEntity.avatarColor
	role = this@toEntity.role
	profileImageKey = this@toEntity.profileImageKey
	status = this@toEntity.status
	deletedAt = this@toEntity.deletedAt
	createdAt = this@toEntity.createdAt
	updatedAt = this@toEntity.updatedAt
}

/**
 * MemberEntity를 Member 도메인 객체로 복원한다.
 */
fun MemberEntity.toDomain(): Member = Member(
	id = MemberId.of(this.id),
	email = this.email?.let { Email(it) },
	name = this.name,
	nickname = this.nickname,
	avatarColor = this.avatarColor,
	role = this.role,
	profileImageKey = this.profileImageKey,
	status = this.status,
	deletedAt = this.deletedAt,
	createdAt = requireNotNull(this.createdAt) { "createdAt must not be null" },
	updatedAt = requireNotNull(this.updatedAt) { "updatedAt must not be null" },
)

// ========== SocialAccountEntity ↔ SocialAccount ==========

/**
 * SocialAccount 도메인 객체를 SocialAccountEntity로 변환한다.
 */
fun SocialAccount.toEntity(): SocialAccountEntity = SocialAccountEntity().apply {
	id = this@toEntity.id.toString()
	memberId = this@toEntity.memberId.toString()
	provider = this@toEntity.provider
	providerId = this@toEntity.providerId
	email = this@toEntity.email
	kakaoNickname = this@toEntity.kakaoNickname
	kakaoProfileImageUrl = this@toEntity.kakaoProfileImageUrl
	createdAt = this@toEntity.createdAt
}

/**
 * SocialAccountEntity를 SocialAccount 도메인 객체로 복원한다.
 */
fun SocialAccountEntity.toDomain(): SocialAccount = SocialAccount(
	id = SocialAccountId.of(this.id),
	memberId = MemberId.of(this.memberId),
	provider = this.provider,
	providerId = this.providerId,
	email = this.email,
	kakaoNickname = this.kakaoNickname,
	kakaoProfileImageUrl = this.kakaoProfileImageUrl,
	createdAt = requireNotNull(this.createdAt) { "createdAt must not be null" },
)
