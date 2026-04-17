package com.unicorn.server.domain.user.port.`in`

import com.unicorn.server.domain.user.User
import com.unicorn.server.domain.user.port.dto.UpdateUserRequest

interface ManageUserInPort {
	fun update(userId: String, request: UpdateUserRequest): User
	fun delete(userId: String)
	fun activate(userId: String)
	fun deactivate(userId: String)
}
