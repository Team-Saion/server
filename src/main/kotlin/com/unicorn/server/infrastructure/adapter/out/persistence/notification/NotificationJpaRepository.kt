package com.unicorn.server.infrastructure.adapter.out.persistence.notification

import com.unicorn.server.domain.notification.enums.NotificationStatus
import com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity.NotificationEntity
import jakarta.persistence.QueryHint
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.QueryHints
import java.time.LocalDateTime

interface NotificationJpaRepository : JpaRepository<NotificationEntity, Long> {
	fun findByDedupKey(dedupKey: String): NotificationEntity?

	@Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
	@QueryHints(QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2"))
	@Query(
		"""
		select n from NotificationEntity n
		where n.status = com.unicorn.server.domain.notification.enums.NotificationStatus.READY
		   or (n.status = com.unicorn.server.domain.notification.enums.NotificationStatus.FAILED and n.nextRetryAt is not null and n.nextRetryAt <= :now)
		order by n.createdAt asc
		""",
	)
	fun findDispatchTargets(now: LocalDateTime, pageable: Pageable): List<NotificationEntity>
}
