package com.unicorn.server.domain.circle.event

import com.unicorn.server.common.domain.Event

class CircleInitiatorTransferredEvent(
	val circleId: String,
	val previousInitiatorMemberId: String,
	val newInitiatorMemberId: String,
) : Event()
