package com.unicorn.server.infrastructure.adapter.`in`.web.user

import com.unicorn.server.domain.user.port.dto.CreateUserRequest
import com.unicorn.server.domain.user.port.dto.UpdateUserRequest
import com.unicorn.server.domain.user.port.dto.UserResponse
import com.unicorn.server.domain.user.port.`in`.GetUserInPort
import com.unicorn.server.domain.user.port.`in`.ManageUserInPort
import com.unicorn.server.domain.user.port.`in`.RegisterUserInPort
import com.unicorn.server.domain.user.vo.UserId
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
	private val registerUserInPort: RegisterUserInPort,
	private val getUserInPort: GetUserInPort,
	private val manageUserInPort: ManageUserInPort,
) {

	@PostMapping
	fun register(@RequestBody @Valid request: CreateUserRequest): ApiResponse<UserResponse> {
		val user = registerUserInPort.register(request)
		return ApiResponse.created(UserResponse.from(user))
	}

	@GetMapping("/{userId}")
	fun getUser(@PathVariable userId: String): ApiResponse<UserResponse> {
		val user = getUserInPort.getById(UserId.of(userId))
		return ApiResponse.success(UserResponse.from(user))
	}

	@PutMapping("/{userId}")
	fun updateUser(
		@PathVariable userId: String,
		@RequestBody @Valid request: UpdateUserRequest,
	): ApiResponse<UserResponse> {
		val user = manageUserInPort.update(userId, request)
		return ApiResponse.success(UserResponse.from(user))
	}

	@DeleteMapping("/{userId}")
	fun deleteUser(@PathVariable userId: String): ApiResponse<Unit> {
		manageUserInPort.delete(userId)
		return ApiResponse.success()
	}

	@PatchMapping("/{userId}/activate")
	fun activateUser(@PathVariable userId: String): ApiResponse<Unit> {
		manageUserInPort.activate(userId)
		return ApiResponse.success()
	}

	@PatchMapping("/{userId}/deactivate")
	fun deactivateUser(@PathVariable userId: String): ApiResponse<Unit> {
		manageUserInPort.deactivate(userId)
		return ApiResponse.success()
	}
}
