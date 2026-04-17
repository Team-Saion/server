package com.unicorn.server.domain.user.port.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Size

data class UpdateUserRequest(
	@field:Size(min = 2, max = 50, message = "Username must be between 2 and 50 characters")
	val username: String?,

	@field:Email(message = "Invalid email format")
	val email: String?,
)
