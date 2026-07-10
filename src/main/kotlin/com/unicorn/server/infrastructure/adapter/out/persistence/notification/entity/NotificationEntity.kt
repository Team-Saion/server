package com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity

import com.unicorn.server.common.persistence.AuditableJpaEntity
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.enums.NotificationEventType
import com.unicorn.server.domain.notification.enums.NotificationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
	name = "notification",
	indexes = [
		Index(name = "idx_notification_status_next_retry_at", columnList = "status,next_retry_at"),
		Index(name = "uk_notification_dedup_key", columnList = "dedup_key", unique = true),
	],
)
open class NotificationEntity internal constructor() : AuditableJpaEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	var id: Long? = null
		internal set

	@Enumerated(EnumType.STRING)
	@Column(name = "channel", nullable = false, length = 20)
	lateinit var channel: NotificationChannel
		internal set

	@Column(name = "receiver", nullable = false, length = 255)
	var receiver: String = ""
		internal set

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 50)
	lateinit var eventType: NotificationEventType
		internal set

	@Column(name = "payload", nullable = false, columnDefinition = "TEXT")
	var payload: String = "{}"
		internal set

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	lateinit var status: NotificationStatus
		internal set

	@Column(name = "attempt_count", nullable = false)
	var attemptCount: Int = 0
		internal set

	@Column(name = "next_retry_at")
	var nextRetryAt: LocalDateTime? = null
		internal set

	@Column(name = "sent_at")
	var sentAt: LocalDateTime? = null
		internal set

	@Column(name = "failed_at")
	var failedAt: LocalDateTime? = null
		internal set

	@Column(name = "fail_reason", length = 500)
	var failReason: String? = null
		internal set

	@Column(name = "dedup_key", nullable = false, length = 255)
	var dedupKey: String = ""
		internal set
}
