package com.unicorn.server.domain.user

import com.unicorn.server.domain.user.enums.UserStatus
import com.unicorn.server.domain.user.vo.Email
import com.unicorn.server.domain.user.vo.UserId
import java.time.LocalDateTime

// User 도메인 - 비즈니스 규칙, 상태 변경 메서드, JPA 모듈 담당
class User private constructor(
	val id: UserId,
	email: Email,
	username: String,
	passwordHash: String,
	status: UserStatus,
	val createdAt: LocalDateTime,
	updatedAt: LocalDateTime,
) {
	var email: Email = email
		private set

	var username: String = username
		private set

	var passwordHash: String = passwordHash
		private set

	var status: UserStatus = status
		private set

	var updatedAt: LocalDateTime = updatedAt
		private set

	fun activate() {
		if (status == UserStatus.ACTIVE) {
			throw IllegalStateException("User is already active")
		}
		if (status == UserStatus.DELETED) {
			throw IllegalStateException("Deleted user cannot be activated")
		}
		status = UserStatus.ACTIVE
		updatedAt = LocalDateTime.now()
	}

	fun deactivate() {
		if (status == UserStatus.DELETED) {
			throw IllegalStateException("Deleted user cannot be deactivated")
		}
		status = UserStatus.INACTIVE
		updatedAt = LocalDateTime.now()
	}

	fun delete() {
		if (status == UserStatus.DELETED) {
			throw IllegalStateException("User is already deleted")
		}
		status = UserStatus.DELETED
		updatedAt = LocalDateTime.now()
	}

	fun changeEmail(newEmail: Email) {
		email = newEmail
		updatedAt = LocalDateTime.now()
	}

	fun changeUsername(newUsername: String) {
		validateUsername(newUsername)
		username = newUsername
		updatedAt = LocalDateTime.now()
	}

	fun changePasswordHash(newPasswordHash: String) {
		validatePasswordHash(newPasswordHash)
		passwordHash = newPasswordHash
		updatedAt = LocalDateTime.now()
	}

	fun isActive(): Boolean = status == UserStatus.ACTIVE

	companion object {
		fun create(email: Email, username: String, passwordHash: String): User {
			validateUsername(username)
			validatePasswordHash(passwordHash)

			val now = LocalDateTime.now()
			return User(UserId.generate(), email, username, passwordHash, UserStatus.PENDING, now, now)
		}

		fun reconstitute(
			id: UserId,
			email: Email,
			username: String,
			passwordHash: String,
			status: UserStatus,
			createdAt: LocalDateTime,
			updatedAt: LocalDateTime,
		): User = User(id, email, username, passwordHash, status, createdAt, updatedAt)

		private fun validateUsername(username: String?) {
			if (username.isNullOrBlank()) {
				throw IllegalArgumentException("Username cannot be blank")
			}
			if (username.length < 2 || username.length > 50) {
				throw IllegalArgumentException("Username must be between 2 and 50 characters")
			}
		}

		private fun validatePasswordHash(passwordHash: String?) {
			if (passwordHash.isNullOrBlank()) {
				throw IllegalArgumentException("Password hash cannot be blank")
			}
		}
	}
}
