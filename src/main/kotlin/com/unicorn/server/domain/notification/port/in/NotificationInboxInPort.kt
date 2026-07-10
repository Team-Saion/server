package com.unicorn.server.domain.notification.port.`in`

import com.unicorn.server.domain.notification.NotificationInboxItem
import com.unicorn.server.domain.notification.port.dto.NotificationInboxPage

interface NotificationInboxInPort {
	fun getInbox(memberId: String, cursor: Long?, size: Int): NotificationInboxPage
	fun markRead(memberId: String, notificationId: Long): NotificationInboxItem
}
