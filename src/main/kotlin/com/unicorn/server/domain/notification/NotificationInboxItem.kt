package com.unicorn.server.domain.notification

import com.unicorn.server.domain.notification.enums.NotificationType
import com.unicorn.server.domain.notification.vo.NotificationInboxItemId
import java.time.LocalDateTime

// NotificationInboxItem 도메인 - 사용자 인앱 알림함의 읽음 상태와 이동 정보
class NotificationInboxItem private constructor(
    // 알림함 아이템 고유 식별자 (신규 생성 시 저장 전까지 null 가능)
    val id: NotificationInboxItemId?,
    // 알림함 소유 멤버 식별자
    val receiverMemberId: String,
    // 알림함 적재 알림 유형
    val type: NotificationType,
    // 알림함 목록 노출 제목
    title: String,
    // 알림함 상세 노출 본문
    body: String,
    // 알림 클릭 시 이동 경로 정보
    val route: NotificationRoute,
    // 알림 생성 근거 이벤트 식별자
    val eventId: String,
    // 동일 알림 중복 적재 방지용 멱등 키
    val dedupKey: String,
    // 사용자의 읽음 처리 시각 (미읽음이면 null)
    readAt: LocalDateTime?,
    // 알림함 아이템 생성 시각
    val createdAt: LocalDateTime,
    // 알림함 아이템 상태 최종 변경 시각
    updatedAt: LocalDateTime,
) {
    var title: String = title
        private set

    var body: String = body
        private set

    var readAt: LocalDateTime? = readAt
        private set

    var updatedAt: LocalDateTime = updatedAt
        private set

    fun markRead(now: LocalDateTime = LocalDateTime.now()) {
        if (readAt != null) {
            return
        }

        readAt = now
        updatedAt = now
    }

    companion object {
        fun create(
            receiverMemberId: String,
            type: NotificationType,
            title: String,
            body: String,
            route: NotificationRoute,
            eventId: String,
            dedupKey: String,
        ): NotificationInboxItem {
            validate(receiverMemberId, title, body, eventId, dedupKey)
            require(type.createsInbox) { "Notification type does not create inbox item" }

            val now = LocalDateTime.now()
            return NotificationInboxItem(
                id = null,
                receiverMemberId = receiverMemberId,
                type = type,
                title = title,
                body = body,
                route = route,
                eventId = eventId,
                dedupKey = dedupKey,
                readAt = null,
                createdAt = now,
                updatedAt = now,
            )
        }

        fun reconstitute(
            id: NotificationInboxItemId,
            receiverMemberId: String,
            type: NotificationType,
            title: String,
            body: String,
            route: NotificationRoute,
            eventId: String,
            dedupKey: String,
            readAt: LocalDateTime?,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime,
        ): NotificationInboxItem {
            validate(receiverMemberId, title, body, eventId, dedupKey)

            return NotificationInboxItem(
                id = id,
                receiverMemberId = receiverMemberId,
                type = type,
                title = title,
                body = body,
                route = route,
                eventId = eventId,
                dedupKey = dedupKey,
                readAt = readAt,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }

        private fun validate(
            receiverMemberId: String,
            title: String,
            body: String,
            eventId: String,
            dedupKey: String,
        ) {
            require(receiverMemberId.isNotBlank()) { "Receiver member id cannot be blank" }
            require(title.isNotBlank()) { "Notification title cannot be blank" }
            require(body.isNotBlank()) { "Notification body cannot be blank" }
            require(eventId.isNotBlank()) { "Event id cannot be blank" }
            require(dedupKey.isNotBlank()) { "Dedup key cannot be blank" }
        }
    }
}
