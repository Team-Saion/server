package com.unicorn.server.common.exception

import org.springframework.http.HttpStatus

enum class CommonErrorCode(
	override val code: String,
	override val message: String,
	override val httpStatus: HttpStatus,
) : ErrorCode {
	// Bad Request
	INVALID_INPUT("G400_1", "Invalid input", HttpStatus.BAD_REQUEST),

	// Server Error
	INTERNAL_SERVER_ERROR("G500_1", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
}
