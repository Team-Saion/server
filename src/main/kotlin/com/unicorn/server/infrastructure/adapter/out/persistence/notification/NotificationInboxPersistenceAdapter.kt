package com.unicorn.server.infrastructure.adapter.out.persistence.notification

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.notification.NotificationInboxItem
import com.unicorn.server.domain.notification.port.out.NotificationInboxOutPort
import com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity.toDomain
import com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity.toEntity
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@PersistenceAdapter
class NotificationInboxPersistenceAdapter(
	private val notificationInboxJpaRepository: NotificationInboxJpaRepository,
) : NotificationInboxOutPort {

	@Transactional
	override fun save(item: NotificationInboxItem): NotificationInboxItem {
		val entity = item.toEntity()
		return notificationInboxJpaRepository.save(entity).toDomain()
	}

	@Transactional(readOnly = true)
	override fun findPageByReceiver(memberId: String, cursor: Long?, limit: Int): List<NotificationInboxItem> {
		val pageable = PageRequest.of(0, limit)
		val entities = cursor?.let { cursorId ->
			notificationInboxJpaRepository.findByReceiverMemberIdAndIdLessThanOrderByCreatedAtDescIdDesc(
				memberId,
				cursorId,
				pageable,
			)
		} ?: notificationInboxJpaRepository.findByReceiverMemberIdOrderByCreatedAtDescIdDesc(memberId, pageable)

		return entities.map { it.toDomain() }
	}

	@Transactional(readOnly = true)
	override fun findByIdAndReceiver(notificationId: Long, memberId: String): NotificationInboxItem? =
		notificationInboxJpaRepository.findByIdAndReceiverMemberId(notificationId, memberId)?.toDomain()

	@Transactional
	override fun deleteCreatedBefore(threshold: LocalDateTime): Int =
		notificationInboxJpaRepository.deleteCreatedBefore(threshold)
}
