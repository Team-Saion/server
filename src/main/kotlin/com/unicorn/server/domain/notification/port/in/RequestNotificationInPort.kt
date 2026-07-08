package com.unicorn.server.domain.notification.port.`in`

import com.unicorn.server.domain.notification.port.dto.RequestNotificationCommand

interface RequestNotificationInPort {
	fun request(command: RequestNotificationCommand)
}
