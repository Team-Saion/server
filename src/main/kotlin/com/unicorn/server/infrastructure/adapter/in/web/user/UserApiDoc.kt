package com.unicorn.server.infrastructure.adapter.`in`.web.user

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.user.exception.UserErrorCode
import com.unicorn.server.domain.user.port.dto.CreateUserRequest
import com.unicorn.server.domain.user.port.dto.UpdateUserRequest
import com.unicorn.server.domain.user.port.dto.UserResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "User API", description = "회원 관련 API")
interface UserApiDoc {

	@Operation(
		summary = "회원 가입",
		description = "이메일, 사용자명, 비밀번호로 신규 회원을 가입합니다.",
	)
	@ApiSuccessCodeExample(UserResponse::class)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = UserErrorCode::class, code = "DUPLICATE_EMAIL"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
	)
	@SwaggerApiResponse(responseCode = "201", description = "회원 가입 성공")
	fun register(
		@RequestBody @Valid request: CreateUserRequest,
	): ApiResponse<UserResponse>

	@Operation(
		summary = "회원 조회",
		description = "회원 ID로 회원 정보를 조회합니다.",
	)
	@ApiSuccessCodeExample(UserResponse::class)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = UserErrorCode::class, code = "USER_NOT_FOUND"),
	)
	@SwaggerApiResponse(responseCode = "200", description = "회원 조회 성공")
	fun getUser(
		@PathVariable userId: String,
	): ApiResponse<UserResponse>

	@Operation(
		summary = "회원 정보 수정",
		description = "회원 ID로 회원의 사용자명 또는 이메일을 수정합니다.",
	)
	@ApiSuccessCodeExample(UserResponse::class)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = UserErrorCode::class, code = "USER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = UserErrorCode::class, code = "DUPLICATE_EMAIL"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
	)
	@SwaggerApiResponse(responseCode = "200", description = "회원 정보 수정 성공")
	fun updateUser(
		@PathVariable userId: String,
		@RequestBody @Valid request: UpdateUserRequest,
	): ApiResponse<UserResponse>

	@Operation(
		summary = "회원 탈퇴",
		description = "회원 ID로 회원을 탈퇴 처리합니다.",
	)
	@ApiSuccessCodeExample
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = UserErrorCode::class, code = "USER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = UserErrorCode::class, code = "USER_ALREADY_DELETED"),
	)
	@SwaggerApiResponse(responseCode = "200", description = "회원 탈퇴 성공")
	fun deleteUser(
		@PathVariable userId: String,
	): ApiResponse<Unit>

	@Operation(
		summary = "회원 활성화",
		description = "회원 ID로 회원 상태를 활성화합니다.",
	)
	@ApiSuccessCodeExample
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = UserErrorCode::class, code = "USER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = UserErrorCode::class, code = "USER_ALREADY_ACTIVE"),
		ApiErrorCodeExample(codeType = UserErrorCode::class, code = "USER_ALREADY_DELETED"),
	)
	@SwaggerApiResponse(responseCode = "200", description = "회원 활성화 성공")
	fun activateUser(
		@PathVariable userId: String,
	): ApiResponse<Unit>

	@Operation(
		summary = "회원 비활성화",
		description = "회원 ID로 회원 상태를 비활성화합니다.",
	)
	@ApiSuccessCodeExample
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = UserErrorCode::class, code = "USER_NOT_FOUND"),
		ApiErrorCodeExample(codeType = UserErrorCode::class, code = "USER_ALREADY_DELETED"),
	)
	@SwaggerApiResponse(responseCode = "200", description = "회원 비활성화 성공")
	fun deactivateUser(
		@PathVariable userId: String,
	): ApiResponse<Unit>
}
