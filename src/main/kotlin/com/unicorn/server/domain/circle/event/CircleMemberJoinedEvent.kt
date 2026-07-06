package com.unicorn.server.domain.circle.event

import com.unicorn.server.common.domain.Event
import com.unicorn.server.domain.circle.enums.CircleRole

class CircleMemberJoinedEvent(
	val circleId: String,
	val memberId: String,
	val role: CircleRole,
) : Event()
