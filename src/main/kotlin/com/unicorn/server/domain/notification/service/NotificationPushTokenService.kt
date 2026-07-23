package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.DevicePushToken
import com.unicorn.server.domain.notification.exception.NotificationNotFoundException
import com.unicorn.server.domain.notification.port.`in`.NotificationPushTokenInPort
import com.unicorn.server.domain.notification.port.dto.RegisterPushTokenCommand
import com.unicorn.server.domain.notification.port.out.NotificationPushTokenOutPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NotificationPushTokenService(
	private val notificationPushTokenOutPort: NotificationPushTokenOutPort,
) : NotificationPushTokenInPort {

	@Transactional
	override fun register(memberId: String, command: RegisterPushTokenCommand): DevicePushToken {
		require(memberId.isNotBlank()) { "Member id cannot be blank" }
		val pushToken = (
			notificationPushTokenOutPort.findByInstallationId(command.installationId)
				?: notificationPushTokenOutPort.findByToken(command.token)
			)?.also { existing ->
			existing.refresh(
				memberId = memberId,
				installationId = command.installationId,
				token = command.token,
				platform = command.platform,
			)
		} ?: DevicePushToken.register(
			memberId = memberId,
			installationId = command.installationId,
			token = command.token,
			platform = command.platform,
		)

		return notificationPushTokenOutPort.save(pushToken)
	}

	@Transactional
	override fun deactivate(memberId: String, tokenId: Long) {
		require(memberId.isNotBlank()) { "Member id cannot be blank" }
		val pushToken = notificationPushTokenOutPort.findByIdAndMemberId(tokenId, memberId)
			?: throw NotificationNotFoundException(tokenId)

		pushToken.deactivate()
		notificationPushTokenOutPort.save(pushToken)
	}

	@Transactional(readOnly = true)
	override fun getActiveReceivable(memberId: String): List<DevicePushToken> =
		notificationPushTokenOutPort.findActiveReceivableByMemberId(memberId)
}
