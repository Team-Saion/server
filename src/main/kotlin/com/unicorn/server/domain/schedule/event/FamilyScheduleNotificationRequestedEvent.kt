package com.unicorn.server.domain.schedule.event

import com.unicorn.server.common.domain.Event

class FamilyScheduleNotificationRequestedEvent(
	val requestId: String,
	val scheduleId: String,
	val circleId: String,
	val senderMemberId: String,
	val scheduleTitle: String,
	val dDay: String,
) : Event()
