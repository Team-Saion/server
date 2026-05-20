package com.unicorn.server.infrastructure.adapter.out.event

import com.unicorn.server.common.domain.Event
import com.unicorn.server.common.port.out.event.EventPublisher
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class SpringEventPublisherAdapter(
	private val applicationEventPublisher: ApplicationEventPublisher,
) : EventPublisher {
	override fun publish(event: Event) {
		applicationEventPublisher.publishEvent(event)
	}
}
