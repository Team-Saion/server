package com.unicorn.server.domain.notification.port.`in`

import com.unicorn.server.domain.notification.port.dto.RequestNotificationCommand

interface NotificationRequestInPort {
	fun request(command: RequestNotificationCommand)
}
