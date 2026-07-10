package com.unicorn.server.infrastructure.adapter.`in`.scheduler.notification

import com.unicorn.server.domain.notification.port.`in`.NotificationDispatchInPort
import com.unicorn.server.infrastructure.config.NotificationProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "app.notification.dispatch", name = ["enabled"], havingValue = "true")
class NotificationDispatchScheduler(
	private val notificationDispatchInPort: NotificationDispatchInPort,
	private val notificationProperties: NotificationProperties,
) {
	@Scheduled(fixedDelayString = "#{@notificationProperties.dispatch.intervalMs}")
	fun dispatch() {
		notificationDispatchInPort.dispatch(notificationProperties.dispatch.batchSize)
	}
}
