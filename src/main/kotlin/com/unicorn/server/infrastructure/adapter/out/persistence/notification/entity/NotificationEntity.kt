package com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.unicorn.server.common.persistence.AuditableJpaEntity
import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.enums.NotificationEventType
import com.unicorn.server.domain.notification.enums.NotificationStatus
import com.unicorn.server.domain.notification.vo.NotificationId
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
open class NotificationEntity protected constructor() : AuditableJpaEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	var id: Long? = null
		protected set

	@Enumerated(EnumType.STRING)
	@Column(name = "channel", nullable = false, length = 20)
	lateinit var channel: NotificationChannel
		protected set

	@Column(name = "receiver", nullable = false, length = 255)
	var receiver: String = ""
		protected set

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 50)
	lateinit var eventType: NotificationEventType
		protected set

	@Column(name = "payload", nullable = false, columnDefinition = "TEXT")
	var payload: String = "{}"
		protected set

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	lateinit var status: NotificationStatus
		protected set

	@Column(name = "attempt_count", nullable = false)
	var attemptCount: Int = 0
		protected set

	@Column(name = "next_retry_at")
	var nextRetryAt: LocalDateTime? = null
		protected set

	@Column(name = "sent_at")
	var sentAt: LocalDateTime? = null
		protected set

	@Column(name = "failed_at")
	var failedAt: LocalDateTime? = null
		protected set

	@Column(name = "fail_reason", length = 500)
	var failReason: String? = null
		protected set

	@Column(name = "dedup_key", nullable = false, length = 255)
	var dedupKey: String = ""
		protected set

	constructor(notification: Notification, payloadJson: String) : this() {
		channel = notification.channel
		receiver = notification.receiver
		eventType = notification.eventType
		payload = payloadJson
		status = notification.status
		attemptCount = notification.attemptCount
		nextRetryAt = notification.nextRetryAt
		sentAt = notification.sentAt
		failedAt = notification.failedAt
		failReason = notification.failReason
		dedupKey = notification.dedupKey
		createdAt = notification.createdAt
		updatedAt = notification.updatedAt
	}

	fun update(notification: Notification, payloadJson: String) {
		channel = notification.channel
		receiver = notification.receiver
		eventType = notification.eventType
		payload = payloadJson
		status = notification.status
		attemptCount = notification.attemptCount
		nextRetryAt = notification.nextRetryAt
		sentAt = notification.sentAt
		failedAt = notification.failedAt
		failReason = notification.failReason
		dedupKey = notification.dedupKey
		updatedAt = notification.updatedAt
	}

	fun toDomain(objectMapper: ObjectMapper, payloadTypeReference: TypeReference<Map<String, String>>): Notification =
		Notification.reconstitute(
			id = NotificationId.of(requireNotNull(id) { "id must not be null" }),
			channel = channel,
			receiver = receiver,
			eventType = eventType,
			payload = objectMapper.readValue(payload, payloadTypeReference),
			dedupKey = dedupKey,
			status = status,
			attemptCount = attemptCount,
			nextRetryAt = nextRetryAt,
			sentAt = sentAt,
			failedAt = failedAt,
			failReason = failReason,
			createdAt = requireNotNull(createdAt) { "createdAt must not be null" },
			updatedAt = requireNotNull(updatedAt) { "updatedAt must not be null" },
		)
}
