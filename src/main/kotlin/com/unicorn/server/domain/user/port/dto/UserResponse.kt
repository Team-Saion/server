package com.unicorn.server.domain.user.port.dto

import com.unicorn.server.domain.user.User
import com.unicorn.server.domain.user.enums.UserStatus
import java.time.LocalDateTime

data class UserResponse(
	val userId: String,
	val email: String,
	val username: String,
	val status: UserStatus,
	val createdAt: LocalDateTime,
	val updatedAt: LocalDateTime,
) {
	companion object {
		fun from(user: User): UserResponse = UserResponse(
			userId = user.id.toString(),
			email = user.email.value,
			username = user.username,
			status = user.status,
			createdAt = user.createdAt,
			updatedAt = user.updatedAt,
		)
	}
}
