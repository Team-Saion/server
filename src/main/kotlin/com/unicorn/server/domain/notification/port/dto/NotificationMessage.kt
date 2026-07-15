package com.unicorn.server.domain.notification.port.dto

import com.unicorn.server.domain.notification.enums.NotificationChannel

interface NotificationMessage {
	val channel: NotificationChannel
}
