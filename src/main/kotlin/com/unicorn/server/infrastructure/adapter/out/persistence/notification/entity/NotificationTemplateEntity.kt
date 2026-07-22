package com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity

import com.unicorn.server.common.persistence.AuditableJpaEntity
import com.unicorn.server.domain.notification.enums.NotificationEventType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
	name = "notification_template",
	indexes = [
		Index(name = "uk_notification_template_event_type", columnList = "event_type", unique = true),
	],
)
open class NotificationTemplateEntity internal constructor() : AuditableJpaEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	var id: Long? = null
		internal set

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 50)
	lateinit var eventType: NotificationEventType
		internal set

	@Column(name = "title_template", nullable = false, length = 100)
	var titleTemplate: String = ""
		internal set

	@Column(name = "body_template", nullable = false, length = 500)
	var bodyTemplate: String = ""
		internal set

	@Column(name = "active", nullable = false)
	var active: Boolean = true
		internal set
}
