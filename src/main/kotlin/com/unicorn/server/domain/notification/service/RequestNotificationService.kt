package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.port.`in`.RequestNotificationInPort
import com.unicorn.server.domain.notification.port.dto.RequestNotificationCommand
import com.unicorn.server.domain.notification.port.out.NotificationStore
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class RequestNotificationService(
	private val notificationStore: NotificationStore,
) : RequestNotificationInPort {

	@Transactional
	override fun request(command: RequestNotificationCommand) {
		if (notificationStore.findByDedupKey(command.dedupKey) != null) {
			return
		}

		notificationStore.save(
			Notification.create(
				channel = command.channel,
				receiver = command.receiver,
				eventType = command.eventType,
				payload = command.payload,
				dedupKey = command.dedupKey,
			),
		)
	}
}
