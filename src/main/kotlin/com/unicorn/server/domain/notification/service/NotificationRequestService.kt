package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.port.`in`.NotificationRequestInPort
import com.unicorn.server.domain.notification.port.dto.RequestNotificationCommand
import com.unicorn.server.domain.notification.port.out.NotificationOutPort
import com.unicorn.server.domain.notification.port.out.NotificationTemplateOutPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NotificationRequestService(
	private val notificationOutPort: NotificationOutPort,
	private val notificationTemplateOutPort: NotificationTemplateOutPort,
) : NotificationRequestInPort {

	@Transactional
	override fun request(command: RequestNotificationCommand) {
		if (notificationOutPort.findByDedupKey(command.dedupKey) != null) {
			return
		}
		val eventType = command.payload.eventType
		val template = requireNotNull(notificationTemplateOutPort.findActiveByEventType(eventType)) {
			"Active notification template not found: eventType=$eventType"
		}
		val renderedPayload = template.renderPayload(command.payload)

		notificationOutPort.save(
			Notification.create(
				channel = command.channel,
				receiver = command.receiver,
				eventType = eventType,
				payload = renderedPayload,
				dedupKey = command.dedupKey,
			),
		)
	}
}
