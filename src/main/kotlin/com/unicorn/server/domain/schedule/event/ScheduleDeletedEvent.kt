package com.unicorn.server.domain.schedule.event

import com.unicorn.server.common.domain.Event

class ScheduleDeletedEvent(
	val scheduleId: String,
	val circleId: String,
	val deletedByMemberId: String,
	val scheduleTitle: String,
) : Event()
