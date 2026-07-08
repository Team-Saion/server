package com.unicorn.server.infrastructure.adapter.`in`.web.common

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.common.port.out.storage.exception.ObjectStorageErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.multipart.MaxUploadSizeExceededException
import org.springframework.web.multipart.support.MissingServletRequestPartException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.resource.NoResourceFoundException

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

	@ExceptionHandler(HttpMessageNotReadableException::class)
	fun handleMessageNotReadable(e: HttpMessageNotReadableException): ApiResponse<Unit> =
		ApiResponse.error(CommonErrorCode.INVALID_INPUT, "Invalid request body")

	@ExceptionHandler(MaxUploadSizeExceededException::class)
	fun handleMaxUploadSizeExceeded(e: MaxUploadSizeExceededException): ApiResponse<Unit> =
		ApiResponse.error(ObjectStorageErrorCode.OBJECT_SIZE_EXCEEDED)

	@ExceptionHandler(MissingServletRequestPartException::class)
	fun handleMissingRequestPart(e: MissingServletRequestPartException): ApiResponse<Unit> =
		ApiResponse.error(CommonErrorCode.INVALID_INPUT, "${e.requestPartName} part is required")
	@ExceptionHandler(NoResourceFoundException::class)
	fun handleNoResourceFoundException(e: NoResourceFoundException): ApiResponse<Unit> =
		ApiResponse.error(CommonErrorCode.NOT_FOUND, e.message ?: CommonErrorCode.NOT_FOUND.message)

	@ExceptionHandler(ResponseStatusException::class)
	fun handleResponseStatusException(e: ResponseStatusException): ApiResponse<Unit> =
		ApiResponse.error(e.statusCode.value().toString(), e.reason ?: e.statusCode.toString(), e.statusCode)

	@ExceptionHandler(Exception::class)
	fun handleException(e: Exception): ApiResponse<Unit> =
		ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR, "An unexpected error occurred")
}
