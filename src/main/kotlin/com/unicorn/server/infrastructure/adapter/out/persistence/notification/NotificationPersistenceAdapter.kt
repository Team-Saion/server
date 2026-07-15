package com.unicorn.server.infrastructure.adapter.out.persistence.notification

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.port.out.NotificationOutPort
import com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity.toDomain
import com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity.toEntity
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@PersistenceAdapter
class NotificationPersistenceAdapter(
	private val notificationJpaRepository: NotificationJpaRepository,
	private val objectMapper: ObjectMapper,
) : NotificationOutPort {
	private val payloadTypeReference = object : TypeReference<Map<String, String>>() {}

	@Transactional
	override fun save(notification: Notification): Notification {
		val entity = notification.toEntity(objectMapper)
		return notificationJpaRepository.save(entity).toDomain(objectMapper, payloadTypeReference)
	}

	@Transactional(readOnly = true)
	override fun findByDedupKey(dedupKey: String): Notification? =
		notificationJpaRepository.findByDedupKey(dedupKey)
			?.toDomain(objectMapper, payloadTypeReference)

	@Transactional
	override fun findDispatchTargets(limit: Int, now: LocalDateTime): List<Notification> =
		notificationJpaRepository.findDispatchTargets(now, PageRequest.of(0, limit))
			.map { it.toDomain(objectMapper, payloadTypeReference) }
}
