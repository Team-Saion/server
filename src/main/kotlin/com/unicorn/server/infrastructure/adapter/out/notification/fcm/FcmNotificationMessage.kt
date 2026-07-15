package com.unicorn.server.infrastructure.adapter.out.notification.fcm

import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.port.dto.NotificationMessage

data class FcmNotificationMessage(
	val token: String,
	val title: String,
	val body: String,
	val data: Map<String, String>,
) : NotificationMessage {
	override val channel: NotificationChannel = NotificationChannel.PUSH
}
