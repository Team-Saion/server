package com.unicorn.server.domain.user.port.`in`

import com.unicorn.server.domain.user.User
import com.unicorn.server.domain.user.vo.UserId

interface GetUserInPort {
	fun getById(userId: UserId): User
	fun findByEmail(email: String): User?
}
