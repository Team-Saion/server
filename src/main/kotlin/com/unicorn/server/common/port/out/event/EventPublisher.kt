package com.unicorn.server.common.port.out.event

import com.unicorn.server.common.domain.Event

interface EventPublisher {
	fun publish(event: Event)
}
