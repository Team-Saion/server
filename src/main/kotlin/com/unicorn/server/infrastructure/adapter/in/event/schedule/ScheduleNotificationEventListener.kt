package com.unicorn.server.infrastructure.adapter.`in`.event.schedule

import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.circle.port.dto.CircleMemberDto
import com.unicorn.server.domain.notification.event.ScheduleCreatedPayload
import com.unicorn.server.domain.notification.event.ScheduleDeletedPayload
import com.unicorn.server.domain.notification.event.ScheduleConfirmationRequestedPayload
import com.unicorn.server.domain.notification.event.ScheduleConfirmedByFamilyPayload
import com.unicorn.server.domain.notification.event.ScheduleFamilyNotificationPayload
import com.unicorn.server.domain.notification.event.ScheduleReminderD1Payload
import com.unicorn.server.domain.notification.event.ScheduleReminderDDayAllDayPayload
import com.unicorn.server.domain.notification.event.ScheduleReminderDDayTimedPayload
import com.unicorn.server.domain.notification.event.ScheduleReminderD7Payload
import com.unicorn.server.domain.notification.port.`in`.NotificationRequestInPort
import com.unicorn.server.domain.notification.port.dto.RequestNotificationCommand
import com.unicorn.server.domain.schedule.event.ScheduleCreatedEvent
import com.unicorn.server.domain.schedule.event.ScheduleDeletedEvent
import com.unicorn.server.domain.schedule.event.ScheduleConfirmationRequestDueEvent
import com.unicorn.server.domain.schedule.event.ScheduleConfirmedEvent
import com.unicorn.server.domain.schedule.event.FamilyScheduleNotificationRequestedEvent
import com.unicorn.server.domain.schedule.enums.ScheduleReminderType
import com.unicorn.server.domain.schedule.event.ScheduleReminderDueEvent
import com.unicorn.server.domain.schedule.port.`in`.ScheduleConfirmationStatusInPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class ScheduleNotificationEventListener(
	private val circleMemberInPort: CircleMemberInPort,
	private val scheduleConfirmationStatusInPort: ScheduleConfirmationStatusInPort,
	private val notificationRequestInPort: NotificationRequestInPort,
) {
	@EventListener
	fun handle(event: ScheduleCreatedEvent) {
		val members = circleMemberInPort.getCircleMembers(event.circleId)
		val actorName = members.nicknameOf(event.creatorMemberId)
		val payload = ScheduleCreatedPayload(actorName, event.scheduleTitle, event.scheduleId)

		members
			.asSequence()
			.filter { it.active && it.memberId != event.creatorMemberId }
			.forEach { member ->
				notificationRequestInPort.request(
					event.commandFor(member.memberId, payload),
				)
			}
	}

	@EventListener
	fun handle(event: ScheduleDeletedEvent) {
		val members = circleMemberInPort.getCircleMembers(event.circleId)
		val payload = ScheduleDeletedPayload(
			actorName = members.nicknameOf(event.deletedByMemberId),
			scheduleTitle = event.scheduleTitle,
			scheduleId = event.scheduleId,
		)

		members.asSequence()
			.filter { it.active }
			.forEach { member ->
				notificationRequestInPort.request(
					RequestNotificationCommand(
						receiverMemberId = member.memberId,
						payload = payload,
						eventId = event.scheduleId,
						circleId = event.circleId,
					),
				)
			}
	}

	@EventListener
	fun handle(event: ScheduleReminderDueEvent) {
		val payload = event.toPayload()

		circleMemberInPort.getCircleMembers(event.circleId)
			.asSequence()
			.filter { it.active }
			.forEach { member ->
				notificationRequestInPort.request(
					RequestNotificationCommand(
						receiverMemberId = member.memberId,
						payload = payload,
						eventId = "${event.reminderType.name.lowercase()}:${event.scheduleId}",
						circleId = event.circleId,
						scheduleId = event.scheduleId,
					),
				)
			}
	}

	@EventListener
	fun handle(event: ScheduleConfirmedEvent) {
		val confirmerName = circleMemberInPort.getCircleMembers(event.circleId).nicknameOf(event.confirmerMemberId)
		val payload = ScheduleConfirmedByFamilyPayload(confirmerName, event.scheduleTitle, event.scheduleId)
		circleMemberInPort.getCircleMembers(event.circleId)
			.asSequence()
			.filter { it.active && it.memberId != event.confirmerMemberId }
			.forEach { member ->
				notificationRequestInPort.request(
					RequestNotificationCommand(
						receiverMemberId = member.memberId,
						payload = payload,
						eventId = "${event.scheduleId}:${event.confirmerMemberId}",
						circleId = event.circleId,
						scheduleId = event.scheduleId,
					),
				)
			}
	}

	@EventListener
	fun handle(event: ScheduleConfirmationRequestDueEvent) {
		val payload = ScheduleConfirmationRequestedPayload(event.scheduleTitle, event.scheduleId)
		circleMemberInPort.getCircleMembers(event.circleId)
			.asSequence()
			.filter { it.active && it.memberId != event.scheduleCreatorMemberId }
			.filterNot { scheduleConfirmationStatusInPort.hasConfirmed(ScheduleId.of(event.scheduleId), it.memberId) }
			.forEach { member ->
				notificationRequestInPort.request(
					RequestNotificationCommand(
						receiverMemberId = member.memberId,
						payload = payload,
						eventId = event.scheduleId,
						circleId = event.circleId,
						scheduleId = event.scheduleId,
					),
				)
			}
	}

	@EventListener
	fun handle(event: FamilyScheduleNotificationRequestedEvent) {
		val members = circleMemberInPort.getCircleMembers(event.circleId)
		val payload = ScheduleFamilyNotificationPayload(
			senderName = members.nicknameOf(event.senderMemberId),
			scheduleTitle = event.scheduleTitle,
			dDay = event.dDay,
			scheduleId = event.scheduleId,
		)

		members
			.asSequence()
			.filter { it.active && it.memberId != event.senderMemberId }
			.forEach { member ->
				notificationRequestInPort.request(
					RequestNotificationCommand(
						receiverMemberId = member.memberId,
						payload = payload,
						eventId = event.requestId,
						circleId = event.circleId,
						scheduleId = event.scheduleId,
					),
				)
			}
	}

	private fun List<CircleMemberDto>.nicknameOf(memberId: String): String =
		firstOrNull { it.memberId == memberId }?.nickname
			?: error("Active circle member not found: memberId=$memberId")

	private fun ScheduleReminderDueEvent.toPayload() = when (reminderType) {
		ScheduleReminderType.D7 -> ScheduleReminderD7Payload(scheduleTitle, scheduleId)
		ScheduleReminderType.D1 -> ScheduleReminderD1Payload(scheduleTitle, scheduleId)
		ScheduleReminderType.DDAY_ALL_DAY -> ScheduleReminderDDayAllDayPayload(scheduleTitle, scheduleId)
		ScheduleReminderType.DDAY_TIMED -> ScheduleReminderDDayTimedPayload(
			scheduleTitle = scheduleTitle,
			startTime = requireNotNull(startTime) { "Timed reminder requires start time" }.format(TIME_FORMATTER),
			scheduleId = scheduleId,
		)
	}

	private fun ScheduleCreatedEvent.commandFor(
		receiverMemberId: String,
		payload: ScheduleCreatedPayload,
	) = RequestNotificationCommand(
		receiverMemberId = receiverMemberId,
		payload = payload,
		eventId = scheduleId,
		circleId = circleId,
		scheduleId = scheduleId,
	)

	companion object {
		private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
	}
}
