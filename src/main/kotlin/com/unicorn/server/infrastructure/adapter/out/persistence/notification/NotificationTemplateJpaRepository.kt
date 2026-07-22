package com.unicorn.server.infrastructure.adapter.out.persistence.notification

import com.unicorn.server.domain.notification.enums.NotificationEventType
import com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity.NotificationTemplateEntity
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationTemplateJpaRepository : JpaRepository<NotificationTemplateEntity, Long> {
	fun findByEventTypeAndActiveTrue(eventType: NotificationEventType): NotificationTemplateEntity?
}
