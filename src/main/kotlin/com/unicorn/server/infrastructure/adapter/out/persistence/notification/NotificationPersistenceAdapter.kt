package com.unicorn.server.infrastructure.adapter.out.persistence.notification

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.port.out.NotificationStore
import com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity.NotificationEntity
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@PersistenceAdapter
class NotificationPersistenceAdapter(
	private val notificationJpaRepository: NotificationJpaRepository,
	private val objectMapper: ObjectMapper,
) : NotificationStore {
	private val payloadTypeReference = object : TypeReference<Map<String, String>>() {}

	@Transactional
	override fun save(notification: Notification): Notification {
		val payloadJson = objectMapper.writeValueAsString(notification.payload)
		val entity = notification.id?.let { notificationId ->
			notificationJpaRepository.findById(notificationId.value)
				.map { it.apply { update(notification, payloadJson) } }
				.orElseGet { NotificationEntity(notification, payloadJson) }
		} ?: NotificationEntity(notification, payloadJson)

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
