package com.unicorn.server.infrastructure.adapter.out.redis.exception

import com.unicorn.server.common.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class RedisErrorCode(
	override val code: String,
	override val message: String,
	override val httpStatus: HttpStatus,
) : ErrorCode {
	// Not Found
	REDIS_KEY_NOT_FOUND("R404_1", "Redis key not found", HttpStatus.NOT_FOUND),

	// Server Error
	REDIS_OPERATION_FAILED("R500_1", "Redis operation failed", HttpStatus.INTERNAL_SERVER_ERROR),

	// Service Unavailable
	REDIS_CONNECTION_FAILED("R503_1", "Redis connection failed", HttpStatus.SERVICE_UNAVAILABLE),
}
