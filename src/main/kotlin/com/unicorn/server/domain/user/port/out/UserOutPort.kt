package com.unicorn.server.domain.user.port.out

import com.unicorn.server.domain.user.User
import com.unicorn.server.domain.user.vo.Email
import com.unicorn.server.domain.user.vo.UserId

interface UserOutPort {
	fun save(user: User): User
	fun findById(userId: UserId): User?
	fun findByEmail(email: Email): User?
	fun existsByEmail(email: Email): Boolean
}
