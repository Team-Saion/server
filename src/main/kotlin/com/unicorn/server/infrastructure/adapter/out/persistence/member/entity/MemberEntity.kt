package com.unicorn.server.infrastructure.adapter.out.persistence.member.entity

import com.unicorn.server.common.persistence.AuditableJpaEntity
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.enums.MemberStatus
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.vo.MemberId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

// MemberEntity - Member 도메인을 JPA 테이블 row로 저장하고 복원한다.
@Entity
@Table(
	name = "member",
	indexes = [Index(name = "idx_member_email", columnList = "email", unique = true)],
)
class MemberEntity protected constructor() : AuditableJpaEntity() {

	@Id
	@Column(name = "id", nullable = false, length = 36)
	var id: String = ""
		protected set

	@Column(name = "email", nullable = false, length = 255)
	var email: String = ""
		protected set

	@Column(name = "name", nullable = false, length = 100)
	var name: String = ""
		protected set

	@Column(name = "nickname", nullable = false, length = 30)
	var nickname: String = ""
		protected set

	@Column(name = "avatar_color", nullable = false, length = 7)
	var avatarColor: String = ""
		protected set

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	lateinit var role: Role
		protected set

	@Column(name = "profile_image_key", nullable = true, length = 512)
	var profileImageKey: String? = null
		protected set

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	lateinit var status: MemberStatus
		protected set

	@Column(name = "deleted_at", nullable = true)
	var deletedAt: LocalDateTime? = null
		protected set

	constructor(member: Member) : this() {
		id = member.id.toString()
		email = member.email.value
		name = member.name
		nickname = member.nickname
		avatarColor = member.avatarColor
		role = member.role
		profileImageKey = member.profileImageKey
		status = member.status
		deletedAt = member.deletedAt
		createdAt = member.createdAt
		updatedAt = member.updatedAt
	}

	// 도메인 변경사항을 기존 영속성 객체에 반영한다.
	fun update(member: Member) {
		email = member.email.value
		name = member.name
		nickname = member.nickname
		avatarColor = member.avatarColor
		role = member.role
		profileImageKey = member.profileImageKey
		status = member.status
		deletedAt = member.deletedAt
		updatedAt = member.updatedAt
	}

	// 영속성 객체를 순수 도메인 객체로 복원한다.
	fun toDomain(): Member = Member.reconstitute(
		id = MemberId.of(id),
		email = Email(email),
		name = name,
		nickname = nickname,
		avatarColor = avatarColor,
		role = role,
		profileImageKey = profileImageKey,
		status = status,
		deletedAt = deletedAt,
		createdAt = requireNotNull(createdAt) { "createdAt must not be null" },
		updatedAt = requireNotNull(updatedAt) { "updatedAt must not be null" },
	)
}
