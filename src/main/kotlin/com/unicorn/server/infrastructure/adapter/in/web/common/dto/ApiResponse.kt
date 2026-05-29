package com.unicorn.server.infrastructure.adapter.`in`.web.common.dto

import com.unicorn.server.common.exception.ErrorCode
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import java.time.LocalDateTime

@Schema(description = "공통 API 응답")
class ApiResponse<T> private constructor(
	body: Body<T>,
	status: HttpStatusCode,
) : ResponseEntity<ApiResponse.Body<T>>(body, status) {
	@Schema(description = "공통 API 응답 바디")
	data class Body<T>(
		@field:Schema(
			description = "요청 성공 여부",
			example = "true",
		)
		val isSuccess: Boolean,

		@field:Schema(
			description = "성공 응답 데이터. 에러 응답에서는 null이다.",
			nullable = true,
		)
		val data: T?,

		@field:Schema(
			description = "에러 코드. 성공 응답에서는 null이다.",
			example = "G400_1",
			nullable = true,
		)
		val errorCode: String?,

		@field:Schema(
			description = "응답 메시지. 성공 응답에서는 null이다.",
			example = "Invalid input",
			nullable = true,
		)
		val message: String?,

		@field:Schema(
			description = "응답 생성 시각",
			example = "2024-01-01T00:00:00",
		)
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
