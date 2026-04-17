package com.unicorn.server.domain.user.port.`in`

import com.unicorn.server.domain.user.User
import com.unicorn.server.domain.user.port.dto.CreateUserRequest

interface RegisterUserInPort {
	fun register(request: CreateUserRequest): User
}
