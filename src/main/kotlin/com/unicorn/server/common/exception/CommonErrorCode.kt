package com.unicorn.server.common.exception

import org.springframework.http.HttpStatus

enum class CommonErrorCode(
	override val code: String,
	override val message: String,
	override val httpStatus: HttpStatus,
) : ErrorCode {
	// Auth
	UNAUTHORIZED("G401", "Authentication is required", HttpStatus.UNAUTHORIZED),
	FORBIDDEN("G403", "Access is denied", HttpStatus.FORBIDDEN),

	// Bad Request
	INVALID_INPUT("G400", "Invalid input", HttpStatus.BAD_REQUEST),

	// Not Found
	NOT_FOUND("G404", "Resource not found", HttpStatus.NOT_FOUND),

	// Server Error
	INTERNAL_SERVER_ERROR("G500", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
}
