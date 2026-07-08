package com.unicorn.server.domain.notification.port.`in`

interface DispatchReadyNotificationsInPort {
	fun dispatch(limit: Int)
}
