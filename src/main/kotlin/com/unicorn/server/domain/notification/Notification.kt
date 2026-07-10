package com.unicorn.server.domain.notification

import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.enums.NotificationEventType
import com.unicorn.server.domain.notification.enums.NotificationStatus
import com.unicorn.server.domain.notification.vo.NotificationId
import java.time.LocalDateTime

// Notification 도메인 - 발송 대상 알림의 전송 상태와 재시도 생명주기
class Notification private constructor(
	// 알림 고유 식별자 (신규 생성 시 저장 전까지 null 가능)
	val id: NotificationId?,
	// 실제 전달 채널
	val channel: NotificationChannel,
	// 알림 수신 대상 식별자
	receiver: String,
	// 알림 생성 원인 비즈니스 이벤트 타입
	val eventType: NotificationEventType,
	// 메시지 조합 및 후처리용 치환 데이터
	payload: Map<String, String>,
	// 동일 알림 중복 생성 방지용 멱등 키
	dedupKey: String,
	// 현재 전송 처리 상태
	status: NotificationStatus,
	// 누적 전송 시도 횟수
	attemptCount: Int,
	// 실패 후 다음 재시도 예정 시각
	nextRetryAt: LocalDateTime?,
	// 최종 발송 성공 시각
	sentAt: LocalDateTime?,
	// 마지막 실패 기록 시각
	failedAt: LocalDateTime?,
	// 마지막 실패 원인 메시지
	failReason: String?,
	// 알림 최초 생성 시각
	val createdAt: LocalDateTime,
	// 알림 상태 최종 변경 시각
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
