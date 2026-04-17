package com.unicorn.server.infrastructure.adapter.out.persistence.user

import com.unicorn.server.infrastructure.adapter.out.persistence.user.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserEntity, String> {
	fun findByEmail(email: String): UserEntity?
	fun existsByEmail(email: String): Boolean
}
