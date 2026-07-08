package com.unicorn.server.domain.notification.port.out

import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.port.dto.NotificationMessage

interface NotificationSender {
	fun channel(): NotificationChannel

	fun send(message: NotificationMessage)
}
