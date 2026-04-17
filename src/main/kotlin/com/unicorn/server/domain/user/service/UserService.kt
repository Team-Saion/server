package com.unicorn.server.domain.user.service

import com.unicorn.server.common.annotation.UseCase
import com.unicorn.server.domain.user.User
import com.unicorn.server.domain.user.event.UserSignedUpEvent
import com.unicorn.server.domain.user.event.UserWithdrawnEvent
import com.unicorn.server.domain.user.exception.DuplicateEmailException
import com.unicorn.server.domain.user.exception.UserNotFoundException
import com.unicorn.server.domain.user.port.dto.CreateUserRequest
import com.unicorn.server.domain.user.port.dto.UpdateUserRequest
import com.unicorn.server.domain.user.port.`in`.GetUserInPort
import com.unicorn.server.domain.user.port.`in`.ManageUserInPort
import com.unicorn.server.domain.user.port.`in`.RegisterUserInPort
import com.unicorn.server.domain.user.port.out.UserOutPort
import com.unicorn.server.domain.user.vo.Email
import com.unicorn.server.domain.user.vo.UserId
import org.springframework.context.ApplicationEventPublisher

@UseCase
class UserService(
	private val userOutPort: UserOutPort,
	private val eventPublisher: ApplicationEventPublisher,
) : RegisterUserInPort, GetUserInPort, ManageUserInPort {

	override fun register(request: CreateUserRequest): User {
		val email = Email(request.email)

		if (userOutPort.existsByEmail(email)) {
			throw DuplicateEmailException(request.email)
		}

		val passwordHash = "{noop}${request.password}"
		val user = User.create(email, request.username, passwordHash)
		val savedUser = userOutPort.save(user)
		eventPublisher.publishEvent(UserSignedUpEvent(savedUser.id.toString(), savedUser.email.value))
		return savedUser
	}

	override fun getById(userId: UserId): User =
		userOutPort.findById(userId) ?: throw UserNotFoundException(userId.toString())

	override fun findByEmail(email: String): User? = userOutPort.findByEmail(Email(email))

	override fun update(userId: String, request: UpdateUserRequest): User {
		val user = userOutPort.findById(UserId.of(userId)) ?: throw UserNotFoundException(userId)

		if (!request.username.isNullOrBlank()) {
			user.changeUsername(request.username)
		}

		if (!request.email.isNullOrBlank()) {
			val newEmail = Email(request.email)
			if (user.email != newEmail && userOutPort.existsByEmail(newEmail)) {
				throw DuplicateEmailException(request.email)
			}
			user.changeEmail(newEmail)
		}

		return userOutPort.save(user)
	}

	override fun delete(userId: String) {
		val user = userOutPort.findById(UserId.of(userId)) ?: throw UserNotFoundException(userId)
		user.delete()
		val savedUser = userOutPort.save(user)
		eventPublisher.publishEvent(UserWithdrawnEvent(savedUser.id.toString()))
	}

	override fun activate(userId: String) {
		val user = userOutPort.findById(UserId.of(userId)) ?: throw UserNotFoundException(userId)
		user.activate()
		userOutPort.save(user)
	}

	override fun deactivate(userId: String) {
		val user = userOutPort.findById(UserId.of(userId)) ?: throw UserNotFoundException(userId)
		user.deactivate()
		userOutPort.save(user)
	}
}
