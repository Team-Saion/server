package com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity

import com.unicorn.server.common.persistence.AuditableJpaEntity
import com.unicorn.server.domain.notification.enums.NotificationRouteType
import com.unicorn.server.domain.notification.enums.NotificationType
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
	name = "notification_inbox_item",
	indexes = [
		Index(name = "idx_notification_inbox_receiver_created_at", columnList = "receiver_member_id,created_at"),
		Index(name = "idx_notification_inbox_created_at", columnList = "created_at"),
		Index(name = "uk_notification_inbox_dedup_key", columnList = "dedup_key", unique = true),
	],
)
open class NotificationInboxItemEntity internal constructor() : AuditableJpaEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	var id: Long? = null
		internal set

	@Column(name = "receiver_member_id", nullable = false, length = 36)
	var receiverMemberId: String = ""
		internal set

	@Enumerated(EnumType.STRING)
	@Column(name = "notification_type", nullable = false, length = 50)
	lateinit var type: NotificationType
		internal set

	@Column(name = "title", nullable = false, length = 100)
	var title: String = ""
		internal set

	@Column(name = "body", nullable = false, length = 500)
	var body: String = ""
		internal set

	@Enumerated(EnumType.STRING)
	@Column(name = "route_type", nullable = false, length = 30)
	lateinit var routeType: NotificationRouteType
		internal set

	@Column(name = "circle_id", length = 36)
	var circleId: String? = null
		internal set

	@Column(name = "schedule_id", length = 36)
	var scheduleId: String? = null
		internal set

	@Column(name = "event_id", nullable = false, length = 255)
	var eventId: String = ""
		internal set

	@Column(name = "dedup_key", nullable = false, length = 255)
	var dedupKey: String = ""
		internal set

	@Column(name = "read_at")
	var readAt: LocalDateTime? = null
		internal set
}
