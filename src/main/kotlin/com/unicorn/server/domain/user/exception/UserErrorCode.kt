package com.unicorn.server.domain.user.exception

import com.unicorn.server.common.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class UserErrorCode(
	override val code: String,
	override val message: String,
	override val httpStatus: HttpStatus,
) : ErrorCode {
	// Not Found
	USER_NOT_FOUND("U404_1", "User not found", HttpStatus.NOT_FOUND),

	// Conflict
	DUPLICATE_EMAIL("U409_1", "Email already exists", HttpStatus.CONFLICT),
	USER_ALREADY_ACTIVE("U409_2", "User is already active", HttpStatus.CONFLICT),

	// Bad Request
	INVALID_PASSWORD("U400_1", "Invalid password", HttpStatus.BAD_REQUEST),

	// Business
	USER_ALREADY_DELETED("U410_1", "User is already deleted", HttpStatus.GONE),
}
