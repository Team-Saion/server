package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.port.`in`.NotificationRequestInPort
import com.unicorn.server.domain.notification.port.dto.RequestNotificationCommand
import com.unicorn.server.domain.notification.port.out.NotificationOutPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NotificationRequestService(
	private val notificationOutPort: NotificationOutPort,
) : NotificationRequestInPort {

	@Transactional
	override fun request(command: RequestNotificationCommand) {
		if (notificationOutPort.findByDedupKey(command.dedupKey) != null) {
			return
		}

		notificationOutPort.save(
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
