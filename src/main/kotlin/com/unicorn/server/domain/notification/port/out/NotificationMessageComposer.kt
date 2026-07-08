package com.unicorn.server.domain.notification.port.out

import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.port.dto.NotificationMessage

interface NotificationMessageComposer {
	fun channel(): NotificationChannel

	fun compose(notification: Notification): NotificationMessage
}
