package com.unicorn.server.domain.schedule.event

import com.unicorn.server.common.domain.Event

class ScheduleConfirmedEvent(
	val scheduleId: String,
	val circleId: String,
	val scheduleCreatorMemberId: String,
	val confirmerMemberId: String,
	val scheduleTitle: String,
) : Event()
