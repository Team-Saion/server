package com.unicorn.server.infrastructure.adapter.out.persistence.notification

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.notification.NotificationSetting
import com.unicorn.server.domain.notification.port.out.NotificationSettingOutPort
import com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity.toDomain
import com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity.toEntity
import org.springframework.transaction.annotation.Transactional

@PersistenceAdapter
class NotificationSettingPersistenceAdapter(
	private val notificationSettingJpaRepository: NotificationSettingJpaRepository,
) : NotificationSettingOutPort {

	@Transactional
	override fun save(setting: NotificationSetting): NotificationSetting {
		val entity = setting.toEntity()
		return notificationSettingJpaRepository.save(entity).toDomain()
	}

	@Transactional(readOnly = true)
	override fun findByMemberId(memberId: String): NotificationSetting? =
		notificationSettingJpaRepository.findById(memberId)
			.map { it.toDomain() }
			.orElse(null)
}
