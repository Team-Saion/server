package com.unicorn.server.domain.notification.port.`in`

import com.unicorn.server.domain.notification.NotificationInboxItem
import com.unicorn.server.domain.notification.port.dto.NotificationInboxPage

interface NotificationInPort {
	fun getNotifications(memberId: String, cursor: Long?, size: Int): NotificationInboxPage
	fun markNotificationAsRead(memberId: String, notificationId: Long): NotificationInboxItem
}
