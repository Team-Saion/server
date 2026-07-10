package com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity

import com.unicorn.server.common.persistence.AuditableJpaEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "notification_setting")
open class NotificationSettingEntity internal constructor() : AuditableJpaEntity() {
	@Id
	@Column(name = "member_id", nullable = false, length = 36)
	var memberId: String = ""
		internal set

	@Column(name = "d7_enabled", nullable = false)
	var d7Enabled: Boolean = true
		internal set

	@Column(name = "d1_enabled", nullable = false)
	var d1Enabled: Boolean = true
		internal set

	@Column(name = "dday_enabled", nullable = false)
	var dDayEnabled: Boolean = true
		internal set

	@Column(name = "family_schedule_check_enabled", nullable = false)
	var familyScheduleCheckEnabled: Boolean = true
		internal set
}
