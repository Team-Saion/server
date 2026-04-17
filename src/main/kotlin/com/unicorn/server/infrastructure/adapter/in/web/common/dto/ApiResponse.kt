package com.unicorn.server.infrastructure.adapter.`in`.web.common.dto

import com.unicorn.server.common.exception.ErrorCode
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime

class ApiResponse<T> private constructor(
	body: Body<T>,
	status: HttpStatusCode,
) : ResponseEntity<ApiResponse.Body<T>>(body, status) {
	data class Body<T>(
		val isSuccess: Boolean,
		val data: T?,
		val errorCode: String?,
		val message: String?,
		val timestamp: LocalDateTime,
	)

	companion object {
		fun <T> success(data: T): ApiResponse<T> = success(data, HttpStatus.OK)

		fun <T> success(data: T, status: HttpStatusCode): ApiResponse<T> =
			ApiResponse(
				body = Body(
					isSuccess = true,
					data = data,
					errorCode = null,
					message = null,
					timestamp = LocalDateTime.now(),
				),
				status = status,
			)

		fun success(): ApiResponse<Unit> = success(Unit)

		fun created(): ApiResponse<Unit> = success(Unit, HttpStatus.CREATED)

		fun <T> created(data: T): ApiResponse<T> = success(data, HttpStatus.CREATED)

		fun <T> error(errorCode: ErrorCode, message: String = errorCode.message): ApiResponse<T> =
			error(errorCode.code, message, errorCode.httpStatus)

		fun <T> error(errorCode: String, message: String, status: HttpStatusCode): ApiResponse<T> =
			ApiResponse(
				body = Body(
					isSuccess = false,
					data = null,
					errorCode = errorCode,
					message = message,
					timestamp = LocalDateTime.now(),
				),
				status = status,
			)
	}
}
