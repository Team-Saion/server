package com.unicorn.server.infrastructure.adapter.out.persistence.notification

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.notification.NotificationTemplate
import com.unicorn.server.domain.notification.enums.NotificationEventType
import com.unicorn.server.domain.notification.port.out.NotificationTemplateOutPort
import com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity.toDomain

@PersistenceAdapter
class NotificationTemplatePersistenceAdapter(
	private val notificationTemplateJpaRepository: NotificationTemplateJpaRepository,
) : NotificationTemplateOutPort {
	override fun findActiveByEventType(eventType: NotificationEventType): NotificationTemplate? =
		notificationTemplateJpaRepository.findByEventTypeAndActiveTrue(eventType)?.toDomain()
}
