package com.unicorn.server.infrastructure.adapter.out.persistence.user.entity

import com.unicorn.server.common.persistence.AuditableJpaEntity
import com.unicorn.server.domain.user.User
import com.unicorn.server.domain.user.enums.UserStatus
import com.unicorn.server.domain.user.vo.Email
import com.unicorn.server.domain.user.vo.UserId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

// User Entity - 인프라 계층, DB Table 매핑, JPA 어노테이션, 영속성 기술 세부사항
@Entity
@Table(
	name = "users",
	indexes = [Index(name = "idx_users_email", columnList = "email", unique = true)],
)
class UserEntity protected constructor() : AuditableJpaEntity() {
	@Id
	@Column(name = "id", nullable = false, length = 36)
	var id: String = ""
		protected set

	@Column(name = "email", nullable = false, unique = true, length = 255)
	var email: String = ""
		protected set

	@Column(name = "username", nullable = false, length = 100)
	var username: String = ""
		protected set

	@Column(name = "password_hash", nullable = false, length = 255)
	var passwordHash: String = ""
		protected set

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	lateinit var status: UserStatus
		protected set

	@Column(name = "domain_created_at", nullable = false)
	lateinit var domainCreatedAt: LocalDateTime
		protected set

	@Column(name = "domain_updated_at", nullable = false)
	lateinit var domainUpdatedAt: LocalDateTime
		protected set

	constructor(user: User) : this() {
		id = user.id.toString()
		email = user.email.value
		username = user.username
		passwordHash = user.passwordHash
		status = user.status
		domainCreatedAt = user.createdAt
		domainUpdatedAt = user.updatedAt
	}

	fun update(user: User) {
		email = user.email.value
		username = user.username
		passwordHash = user.passwordHash
		status = user.status
		domainUpdatedAt = user.updatedAt
	}

	fun toDomain(): User = User.reconstitute(
		id = UserId.of(id),
		email = Email(email),
		username = username,
		passwordHash = passwordHash,
		status = status,
		createdAt = domainCreatedAt,
		updatedAt = domainUpdatedAt,
	)
}
