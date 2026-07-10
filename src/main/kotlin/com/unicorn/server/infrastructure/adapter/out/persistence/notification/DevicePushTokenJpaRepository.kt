package com.unicorn.server.infrastructure.adapter.out.persistence.notification

import com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity.DevicePushTokenEntity
import org.springframework.data.jpa.repository.JpaRepository

interface DevicePushTokenJpaRepository : JpaRepository<DevicePushTokenEntity, Long> {
	fun findByToken(token: String): DevicePushTokenEntity?

	fun findByIdAndMemberId(id: Long, memberId: String): DevicePushTokenEntity?

	fun findAllByMemberIdAndActiveTrueAndOsNotificationPermissionGrantedTrue(memberId: String): List<DevicePushTokenEntity>
}
