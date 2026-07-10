package com.unicorn.server.infrastructure.adapter.`in`.event.notification

import com.unicorn.server.domain.notification.event.NotificationRequestedEvent
import com.unicorn.server.domain.notification.port.dto.RequestNotificationCommand
import com.unicorn.server.domain.notification.port.`in`.NotificationRequestInPort
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class NotificationRequestedEventListener(
    private val notificationRequestInPort: NotificationRequestInPort,
) {
    @EventListener
    fun handle(event: NotificationRequestedEvent) {
        notificationRequestInPort.request(
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
