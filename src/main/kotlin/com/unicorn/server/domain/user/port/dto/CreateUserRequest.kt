package com.unicorn.server.domain.user.port.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateUserRequest(
	@field:NotBlank(message = "Email is required")
	@field:Email(message = "Invalid email format")
	val email: String,

	@field:NotBlank(message = "Username is required")
	@field:Size(min = 2, max = 50, message = "Username must be between 2 and 50 characters")
	val username: String,

	@field:NotBlank(message = "Password is required")
	@field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
	val password: String,
)
