package com.unicorn.server.domain.schedule.event

import com.unicorn.server.common.domain.Event
import com.unicorn.server.domain.schedule.enums.ScheduleReminderType
import java.time.LocalTime

class ScheduleReminderDueEvent(
	val reminderType: ScheduleReminderType,
	val scheduleId: String,
	val circleId: String,
	val scheduleTitle: String,
	val startTime: LocalTime?,
) : Event()
