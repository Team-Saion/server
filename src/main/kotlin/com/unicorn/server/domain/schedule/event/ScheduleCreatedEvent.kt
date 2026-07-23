package com.unicorn.server.domain.schedule.event

import com.unicorn.server.common.domain.Event

class ScheduleCreatedEvent(
	val scheduleId: String,
	val circleId: String,
	val creatorMemberId: String,
	val scheduleTitle: String,
) : Event()
