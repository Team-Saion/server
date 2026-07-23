package com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity

import com.unicorn.server.common.persistence.AuditableJpaEntity
import com.unicorn.server.domain.notification.enums.DevicePlatform
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
	name = "device_push_token",
	indexes = [
		Index(name = "idx_device_push_token_member_active", columnList = "member_id,active"),
		Index(name = "uk_device_push_token_installation_id", columnList = "installation_id", unique = true),
		Index(name = "uk_device_push_token_token", columnList = "token", unique = true),
	],
)
open class DevicePushTokenEntity internal constructor() : AuditableJpaEntity() {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	var id: Long? = null
		internal set

	@Column(name = "member_id", nullable = false, length = 36)
	var memberId: String = ""
		internal set

	@Column(name = "installation_id", nullable = false, length = 255)
	var installationId: String = ""
		internal set

	@Column(name = "token", nullable = false, length = 512)
	var token: String = ""
		internal set

	@Enumerated(EnumType.STRING)
	@Column(name = "platform", nullable = false, length = 20)
	lateinit var platform: DevicePlatform
		internal set

	@Column(name = "active", nullable = false)
	var active: Boolean = true
		internal set

	@Column(name = "last_seen_at", nullable = false)
	lateinit var lastSeenAt: LocalDateTime
		internal set

	@Column(name = "invalidated_at")
	var invalidatedAt: LocalDateTime? = null
		internal set
}
