package com.unicorn.server.domain.notification.port.`in`

interface NotificationDispatchInPort {
	fun dispatch(limit: Int)
}
