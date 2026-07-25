package com.unicorn.server.domain.notification.port.`in`

import com.unicorn.server.domain.notification.port.dto.PublishNotificationCommand

interface NotificationPublishInPort {
	fun publish(command: PublishNotificationCommand)
}
