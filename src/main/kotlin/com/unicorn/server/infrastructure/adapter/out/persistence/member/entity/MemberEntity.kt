package com.unicorn.server.infrastructure.adapter.out.persistence.member.entity

import com.unicorn.server.common.persistence.AuditableJpaEntity
import com.unicorn.server.domain.member.enums.AvatarColor
import com.unicorn.server.domain.member.enums.MemberStatus
import com.unicorn.server.domain.member.enums.Role
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
class MemberEntity internal constructor() : AuditableJpaEntity() {

	@Id
	@Column(name = "id", nullable = false, length = 36)
	var id: String = ""
		internal set

	@Column(name = "email", nullable = true, length = 255)
	var email: String? = null
		internal set

	@Column(name = "name", nullable = true, length = 100)
	var name: String? = null
		internal set

	@Column(name = "nickname", nullable = false, length = 30)
	var nickname: String = ""
		internal set

	@Enumerated(EnumType.STRING)
	@Column(name = "avatar_color", nullable = false, length = 20)
	lateinit var avatarColor: AvatarColor
		internal set

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false, length = 20)
	lateinit var role: Role
		internal set

	@Column(name = "profile_image_key", nullable = true, length = 512)
	var profileImageKey: String? = null
		internal set

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	lateinit var status: MemberStatus
		internal set

	@Column(name = "deleted_at", nullable = true)
	var deletedAt: LocalDateTime? = null
		internal set


}
