package com.unicorn.server.infrastructure.adapter.`in`.event.notification

import com.unicorn.server.domain.notification.event.NotificationRequestedEvent
import com.unicorn.server.domain.notification.port.`in`.RequestNotificationInPort
import com.unicorn.server.domain.notification.port.dto.RequestNotificationCommand
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class NotificationRequestedEventListener(
	private val requestNotificationInPort: RequestNotificationInPort,
) {
	@EventListener
	fun handle(event: NotificationRequestedEvent) {
		requestNotificationInPort.request(
			RequestNotificationCommand(
				channel = event.channel,
				receiver = event.receiver,
				eventType = event.eventType,
				payload = event.payload,
				dedupKey = event.dedupKey,
			),
		)
	}
}
