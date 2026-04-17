package com.unicorn.server.infrastructure.adapter.out.persistence.user

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.user.User
import com.unicorn.server.domain.user.port.out.UserOutPort
import com.unicorn.server.domain.user.vo.Email
import com.unicorn.server.domain.user.vo.UserId
import com.unicorn.server.infrastructure.adapter.out.persistence.user.mapper.UserMapper
import org.springframework.transaction.annotation.Transactional

// 유저 관련 영속성 컨텍스트를 관리한다.
// 트랜잭션의 경계
@PersistenceAdapter
class UserPersistenceAdapter(
	private val userJpaRepository: UserJpaRepository,
	private val userMapper: UserMapper,
) : UserOutPort {

	@Transactional
	override fun save(user: User): User {
		val entity = userJpaRepository.findById(user.id.toString())
			.map { existing -> existing.apply { userMapper.updateEntity(this, user) } }
			.orElseGet { userMapper.toEntity(user) }

		return userMapper.toDomain(userJpaRepository.save(entity))
	}

	@Transactional(readOnly = true)
	override fun findById(userId: UserId): User? =
		userJpaRepository.findById(userId.toString())
			.map(userMapper::toDomain)
			.orElse(null)

	@Transactional(readOnly = true)
	override fun findByEmail(email: Email): User? = userJpaRepository.findByEmail(email.value)?.let(userMapper::toDomain)

	@Transactional(readOnly = true)
	override fun existsByEmail(email: Email): Boolean = userJpaRepository.existsByEmail(email.value)
}
