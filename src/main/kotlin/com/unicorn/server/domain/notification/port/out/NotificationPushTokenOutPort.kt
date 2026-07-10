package com.unicorn.server.domain.notification.port.out

import com.unicorn.server.domain.notification.DevicePushToken

interface NotificationPushTokenOutPort {
	fun save(pushToken: DevicePushToken): DevicePushToken

	fun findByToken(token: String): DevicePushToken?

	fun findByIdAndMemberId(tokenId: Long, memberId: String): DevicePushToken?

	fun findActiveReceivableByMemberId(memberId: String): List<DevicePushToken>
}
