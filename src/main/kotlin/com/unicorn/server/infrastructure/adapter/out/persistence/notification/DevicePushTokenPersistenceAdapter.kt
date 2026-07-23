package com.unicorn.server.infrastructure.adapter.out.persistence.notification

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.notification.DevicePushToken
import com.unicorn.server.domain.notification.port.out.NotificationPushTokenOutPort
import com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity.toDomain
import com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity.toEntity
import org.springframework.transaction.annotation.Transactional

@PersistenceAdapter
class DevicePushTokenPersistenceAdapter(
	private val devicePushTokenJpaRepository: DevicePushTokenJpaRepository,
) : NotificationPushTokenOutPort {

	@Transactional
	override fun save(pushToken: DevicePushToken): DevicePushToken {
		val entity = pushToken.toEntity()
		return devicePushTokenJpaRepository.save(entity).toDomain()
	}

	@Transactional(readOnly = true)
	override fun findByInstallationId(installationId: String): DevicePushToken? =
		devicePushTokenJpaRepository.findByInstallationId(installationId)?.toDomain()

	@Transactional(readOnly = true)
	override fun findByToken(token: String): DevicePushToken? =
		devicePushTokenJpaRepository.findByToken(token)?.toDomain()

	@Transactional(readOnly = true)
	override fun findByIdAndMemberId(tokenId: Long, memberId: String): DevicePushToken? =
		devicePushTokenJpaRepository.findByIdAndMemberId(tokenId, memberId)?.toDomain()

	@Transactional(readOnly = true)
	override fun findActiveReceivableByMemberId(memberId: String): List<DevicePushToken> =
		devicePushTokenJpaRepository.findAllByMemberIdAndActiveTrue(memberId)
			.map { it.toDomain() }
}
