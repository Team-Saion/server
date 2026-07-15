package com.unicorn.server.domain.notification.port.`in`

import com.unicorn.server.domain.notification.DevicePushToken
import com.unicorn.server.domain.notification.port.dto.RegisterPushTokenCommand

interface NotificationPushTokenInPort {
	fun register(memberId: String, command: RegisterPushTokenCommand): DevicePushToken
	fun deactivate(memberId: String, tokenId: Long)
}
