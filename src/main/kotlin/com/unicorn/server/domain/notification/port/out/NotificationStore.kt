package com.unicorn.server.domain.notification.port.out

import com.unicorn.server.domain.notification.Notification
import java.time.LocalDateTime

interface NotificationStore {
	fun save(notification: Notification): Notification

	fun findByDedupKey(dedupKey: String): Notification?

	fun findDispatchTargets(limit: Int, now: LocalDateTime): List<Notification>
}
