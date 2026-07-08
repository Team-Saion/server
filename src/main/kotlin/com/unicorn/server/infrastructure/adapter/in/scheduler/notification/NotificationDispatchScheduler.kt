package com.unicorn.server.infrastructure.adapter.`in`.scheduler.notification

import com.unicorn.server.domain.notification.port.`in`.DispatchReadyNotificationsInPort
import com.unicorn.server.infrastructure.config.NotificationProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "app.notification.dispatch", name = ["enabled"], havingValue = "true")
class NotificationDispatchScheduler(
	private val dispatchReadyNotificationsInPort: DispatchReadyNotificationsInPort,
	private val notificationProperties: NotificationProperties,
) {
	@Scheduled(fixedDelayString = "#{@notificationProperties.dispatch.intervalMs}")
	fun dispatch() {
		dispatchReadyNotificationsInPort.dispatch(notificationProperties.dispatch.batchSize)
	}
}
