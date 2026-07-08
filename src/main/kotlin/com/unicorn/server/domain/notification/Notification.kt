package com.unicorn.server.domain.notification

import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.enums.NotificationEventType
import com.unicorn.server.domain.notification.enums.NotificationStatus
import com.unicorn.server.domain.notification.vo.NotificationId
import java.time.LocalDateTime

class Notification private constructor(
	val id: NotificationId?,
	val channel: NotificationChannel,
	receiver: String,
	val eventType: NotificationEventType,
	payload: Map<String, String>,
	dedupKey: String,
	status: NotificationStatus,
	attemptCount: Int,
	nextRetryAt: LocalDateTime?,
	sentAt: LocalDateTime?,
	failedAt: LocalDateTime?,
	failReason: String?,
	val createdAt: LocalDateTime,
	updatedAt: LocalDateTime,
) {
	var receiver: String = receiver
		private set

	var payload: Map<String, String> = payload.toMap()
		private set

	var dedupKey: String = dedupKey
		private set

	var status: NotificationStatus = status
		private set

	var attemptCount: Int = attemptCount
		private set

	var nextRetryAt: LocalDateTime? = nextRetryAt
		private set

	var sentAt: LocalDateTime? = sentAt
		private set

	var failedAt: LocalDateTime? = failedAt
		private set

	var failReason: String? = failReason
		private set

	var updatedAt: LocalDateTime = updatedAt
		private set

	fun isDispatchable(now: LocalDateTime): Boolean = when (status) {
		NotificationStatus.READY -> true
		NotificationStatus.FAILED -> nextRetryAt?.let { !it.isAfter(now) } == true
		else -> false
	}

	fun markProcessing(now: LocalDateTime = LocalDateTime.now()) {
		require(isDispatchable(now)) { "Notification is not dispatchable" }

		status = NotificationStatus.PROCESSING
		attemptCount += 1
		failReason = null
		failedAt = null
		nextRetryAt = null
		updatedAt = now
	}

	fun markSent(now: LocalDateTime = LocalDateTime.now()) {
		require(status == NotificationStatus.PROCESSING) { "Notification is not processing" }

		status = NotificationStatus.SENT
		sentAt = now
		failedAt = null
		failReason = null
		nextRetryAt = null
		updatedAt = now
	}

	fun markFailed(reason: String, retryAt: LocalDateTime, now: LocalDateTime = LocalDateTime.now()) {
		require(status == NotificationStatus.PROCESSING) { "Notification is not processing" }
		require(reason.isNotBlank()) { "Failure reason cannot be blank" }

		status = NotificationStatus.FAILED
		failedAt = now
		failReason = reason
		nextRetryAt = retryAt
		updatedAt = now
	}

	fun markDead(reason: String, now: LocalDateTime = LocalDateTime.now()) {
		require(status == NotificationStatus.PROCESSING || status == NotificationStatus.FAILED) {
			"Notification is not failed or processing"
		}
		require(reason.isNotBlank()) { "Failure reason cannot be blank" }

		status = NotificationStatus.DEAD
		failedAt = now
		failReason = reason
		nextRetryAt = null
		updatedAt = now
	}

	companion object {
		fun create(
			channel: NotificationChannel,
			receiver: String,
			eventType: NotificationEventType,
			payload: Map<String, String>,
			dedupKey: String,
		): Notification {
			validateReceiver(receiver)
			validatePayload(payload)
			validateDedupKey(dedupKey)

			val now = LocalDateTime.now()
			return Notification(
				id = null,
				channel = channel,
				receiver = receiver,
				eventType = eventType,
				payload = payload,
				dedupKey = dedupKey,
				status = NotificationStatus.READY,
				attemptCount = 0,
				nextRetryAt = null,
				sentAt = null,
				failedAt = null,
				failReason = null,
				createdAt = now,
				updatedAt = now,
			)
		}

		fun reconstitute(
			id: NotificationId,
			channel: NotificationChannel,
			receiver: String,
			eventType: NotificationEventType,
			payload: Map<String, String>,
			dedupKey: String,
			status: NotificationStatus,
			attemptCount: Int,
			nextRetryAt: LocalDateTime?,
			sentAt: LocalDateTime?,
			failedAt: LocalDateTime?,
			failReason: String?,
			createdAt: LocalDateTime,
			updatedAt: LocalDateTime,
		): Notification {
			validateReceiver(receiver)
			validatePayload(payload)
			validateDedupKey(dedupKey)

			return Notification(
				id = id,
				channel = channel,
				receiver = receiver,
				eventType = eventType,
				payload = payload,
				dedupKey = dedupKey,
				status = status,
				attemptCount = attemptCount,
				nextRetryAt = nextRetryAt,
				sentAt = sentAt,
				failedAt = failedAt,
				failReason = failReason,
				createdAt = createdAt,
				updatedAt = updatedAt,
			)
		}

		private fun validateReceiver(receiver: String) {
			require(receiver.isNotBlank()) { "Receiver cannot be blank" }
		}

		private fun validatePayload(payload: Map<String, String>) {
			require(payload.isNotEmpty()) { "Payload cannot be empty" }
		}

		private fun validateDedupKey(dedupKey: String) {
			require(dedupKey.isNotBlank()) { "Dedup key cannot be blank" }
		}
	}
}
