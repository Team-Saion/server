package com.unicorn.server.infrastructure.adapter.out.persistence.user.mapper

import com.unicorn.server.domain.user.User
import com.unicorn.server.infrastructure.adapter.out.persistence.user.entity.UserEntity
import org.springframework.stereotype.Component

@Component
class UserMapper {
	fun toEntity(user: User): UserEntity = UserEntity(user)
	fun toDomain(entity: UserEntity): User = entity.toDomain()
	fun updateEntity(entity: UserEntity, user: User) = entity.update(user)
}
