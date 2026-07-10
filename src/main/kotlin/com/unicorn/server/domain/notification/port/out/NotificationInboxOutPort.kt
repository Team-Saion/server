package com.unicorn.server.domain.notification.port.out

import com.unicorn.server.domain.notification.NotificationInboxItem
import java.time.LocalDateTime

interface NotificationInboxOutPort {
	fun save(item: NotificationInboxItem): NotificationInboxItem

	fun findPageByReceiver(memberId: String, cursor: Long?, limit: Int): List<NotificationInboxItem>

	fun findByIdAndReceiver(notificationId: Long, memberId: String): NotificationInboxItem?

	fun deleteCreatedBefore(threshold: LocalDateTime): Int
}
