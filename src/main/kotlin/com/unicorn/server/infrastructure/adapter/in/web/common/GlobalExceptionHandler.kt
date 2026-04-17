package com.unicorn.server.infrastructure.adapter.`in`.web.common

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
	@ExceptionHandler(BusinessException::class)
	fun handleBusinessException(e: BusinessException): ApiResponse<Unit> =
		ApiResponse.error(e.errorCode, e.message ?: e.errorCode.message)

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidationException(e: MethodArgumentNotValidException): ApiResponse<Unit> {
		val message = e.bindingResult.fieldErrors
			.map { error -> "${error.field}: ${error.defaultMessage}" }
			.firstOrNull()
			?: "Validation failed"

		return ApiResponse.error(CommonErrorCode.INVALID_INPUT, message)
	}

	@ExceptionHandler(Exception::class)
	fun handleException(e: Exception): ApiResponse<Unit> =
		ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR, "An unexpected error occurred")
}
