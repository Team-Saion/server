package com.unicorn.server.domain.notification.event

import com.unicorn.server.domain.notification.enums.NotificationEventType

sealed interface NotificationEventPayload {
	val eventType: NotificationEventType

	fun toVariables(): Map<String, String>
}

data class CircleJoinCompletedPayload(
	val memberName: String,
	val circleName: String,
) : NotificationEventPayload {
	override val eventType: NotificationEventType = NotificationEventType.CIRCLE_JOIN_COMPLETED

	override fun toVariables(): Map<String, String> = mapOf(
		"member_name" to memberName,
		"circle_name" to circleName,
	)
}

data class ScheduleCreatedPayload(
	val actorName: String,
	val scheduleTitle: String,
) : NotificationEventPayload {
	override val eventType: NotificationEventType = NotificationEventType.SCHEDULE_CREATED

	override fun toVariables(): Map<String, String> = mapOf(
		"actor_name" to actorName,
		"schedule_title" to scheduleTitle,
	)
}

data class ScheduleDeletedPayload(
	val actorName: String,
	val scheduleTitle: String,
) : NotificationEventPayload {
	override val eventType: NotificationEventType = NotificationEventType.SCHEDULE_DELETED

	override fun toVariables(): Map<String, String> = mapOf(
		"actor_name" to actorName,
		"schedule_title" to scheduleTitle,
	)
}

data class ScheduleReminderD7Payload(
	val scheduleTitle: String,
) : NotificationEventPayload {
	override val eventType: NotificationEventType = NotificationEventType.SCHEDULE_REMINDER_D7

	override fun toVariables(): Map<String, String> = mapOf("schedule_title" to scheduleTitle)
}

data class ScheduleReminderD1Payload(
	val scheduleTitle: String,
) : NotificationEventPayload {
	override val eventType: NotificationEventType = NotificationEventType.SCHEDULE_REMINDER_D1

	override fun toVariables(): Map<String, String> = mapOf("schedule_title" to scheduleTitle)
}

data class ScheduleReminderDDayAllDayPayload(
	val scheduleTitle: String,
) : NotificationEventPayload {
	override val eventType: NotificationEventType = NotificationEventType.SCHEDULE_REMINDER_DDAY_ALL_DAY

	override fun toVariables(): Map<String, String> = mapOf("schedule_title" to scheduleTitle)
}

data class ScheduleReminderDDayTimedPayload(
	val scheduleTitle: String,
	val startTime: String,
) : NotificationEventPayload {
	override val eventType: NotificationEventType = NotificationEventType.SCHEDULE_REMINDER_DDAY_TIMED

	override fun toVariables(): Map<String, String> = mapOf(
		"schedule_title" to scheduleTitle,
		"start_time" to startTime,
	)
}

data class ScheduleConfirmedByFamilyPayload(
	val memberName: String,
	val scheduleTitle: String,
) : NotificationEventPayload {
	override val eventType: NotificationEventType = NotificationEventType.SCHEDULE_CONFIRMED_BY_FAMILY

	override fun toVariables(): Map<String, String> = mapOf(
		"member_name" to memberName,
		"schedule_title" to scheduleTitle,
	)
}

data class ScheduleConfirmationRequestedPayload(
	val scheduleTitle: String,
) : NotificationEventPayload {
	override val eventType: NotificationEventType = NotificationEventType.SCHEDULE_CONFIRMATION_REQUESTED

	override fun toVariables(): Map<String, String> = mapOf("schedule_title" to scheduleTitle)
}
