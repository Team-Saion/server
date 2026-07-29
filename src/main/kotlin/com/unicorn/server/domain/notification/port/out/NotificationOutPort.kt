package com.unicorn.server.domain.notification.port.out

import com.unicorn.server.domain.notification.Notification
import java.time.LocalDateTime

interface NotificationOutPort {
	fun save(notification: Notification): Notification

	fun findByDedupKey(dedupKey: String): Notification?

	fun claimDispatchTargets(limit: Int, now: LocalDateTime): List<Notification>
}
