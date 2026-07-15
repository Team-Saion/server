package com.unicorn.server.infrastructure.adapter.out.persistence.notification

import com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity.NotificationInboxItemEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

interface NotificationInboxJpaRepository : JpaRepository<NotificationInboxItemEntity, Long> {
	fun findByReceiverMemberIdOrderByIdDesc(
		receiverMemberId: String,
		pageable: Pageable,
	): List<NotificationInboxItemEntity>

	fun findByReceiverMemberIdAndIdLessThanOrderByIdDesc(
		receiverMemberId: String,
		id: Long,
		pageable: Pageable,
	): List<NotificationInboxItemEntity>

	fun findByIdAndReceiverMemberId(id: Long, receiverMemberId: String): NotificationInboxItemEntity?

	@Modifying
	@Query("delete from NotificationInboxItemEntity n where n.createdAt < :threshold")
	fun deleteCreatedBefore(threshold: LocalDateTime): Int
}
