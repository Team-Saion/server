package com.unicorn.server.domain.schedule.event

import com.unicorn.server.common.domain.Event

class ScheduleConfirmationRequestDueEvent(
	val scheduleId: String,
	val circleId: String,
	val scheduleCreatorMemberId: String,
	val scheduleTitle: String,
) : Event()
