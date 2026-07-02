package com.unicorn.server.domain.circle.event

import com.unicorn.server.common.domain.Event

class CircleCreatedEvent(
	val circleId: String,
	val ownerId: String,
) : Event()
