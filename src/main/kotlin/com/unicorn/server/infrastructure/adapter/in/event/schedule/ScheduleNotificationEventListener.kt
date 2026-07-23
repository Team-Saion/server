package com.unicorn.server.infrastructure.adapter.`in`.event.schedule

import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.circle.port.dto.CircleMemberDto
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.enums.NotificationSettingType
import com.unicorn.server.domain.notification.event.NotificationRequestedEvent
import com.unicorn.server.domain.notification.event.ScheduleCreatedPayload
import com.unicorn.server.domain.notification.event.ScheduleConfirmationRequestedPayload
import com.unicorn.server.domain.notification.event.ScheduleConfirmedByFamilyPayload
import com.unicorn.server.domain.notification.event.ScheduleReminderD1Payload
import com.unicorn.server.domain.notification.event.ScheduleReminderDDayAllDayPayload
import com.unicorn.server.domain.notification.event.ScheduleReminderDDayTimedPayload
import com.unicorn.server.domain.notification.event.ScheduleReminderD7Payload
import com.unicorn.server.domain.notification.port.`in`.NotificationPushTokenInPort
import com.unicorn.server.domain.notification.port.`in`.NotificationSettingInPort
import com.unicorn.server.domain.schedule.event.ScheduleCreatedEvent
import com.unicorn.server.domain.schedule.event.ScheduleConfirmationRequestDueEvent
import com.unicorn.server.domain.schedule.event.ScheduleConfirmedEvent
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
	private val notificationPushTokenInPort: NotificationPushTokenInPort,
	private val notificationSettingInPort: NotificationSettingInPort,
	private val scheduleConfirmationStatusInPort: ScheduleConfirmationStatusInPort,
	private val eventPublisher: EventPublisher,
) {
	@EventListener
	fun handle(event: ScheduleCreatedEvent) {
		val members = circleMemberInPort.getCircleMembers(event.circleId)
		val actorName = members.nicknameOf(event.creatorMemberId)
		val payload = ScheduleCreatedPayload(actorName, event.scheduleTitle)

		members
			.asSequence()
			.filter { it.active }
			.forEach { member ->
				val receiverDedupKey = "schedule-created:${event.scheduleId}:${member.memberId}"
				notificationPushTokenInPort.getActiveReceivable(member.memberId).forEach { pushToken ->
					eventPublisher.publish(
						NotificationRequestedEvent(
							channel = NotificationChannel.PUSH,
							receiver = pushToken.token,
							payload = payload,
							dedupKey = "$receiverDedupKey:token:${requireNotNull(pushToken.id).value}",
						),
					)
				}
			}
	}

	@EventListener
	fun handle(event: ScheduleReminderDueEvent) {
		val payload = event.toPayload()
		val settingType = event.reminderType.toSettingType()

		circleMemberInPort.getCircleMembers(event.circleId)
			.asSequence()
			.filter { it.active }
			.filter { notificationSettingInPort.getSetting(it.memberId).isEnabled(settingType) }
			.forEach { member ->
				val receiverDedupKey = "schedule-reminder:${event.reminderType.name.lowercase()}:${event.scheduleId}:${member.memberId}"
				notificationPushTokenInPort.getActiveReceivable(member.memberId).forEach { pushToken ->
					eventPublisher.publish(
						NotificationRequestedEvent(
							channel = NotificationChannel.PUSH,
							receiver = pushToken.token,
							payload = payload,
							dedupKey = "$receiverDedupKey:token:${requireNotNull(pushToken.id).value}",
						),
					)
				}
			}
	}

	@EventListener
	fun handle(event: ScheduleConfirmedEvent) {
		if (event.scheduleCreatorMemberId == event.confirmerMemberId) {
			return
		}
		if (!notificationSettingInPort.getSetting(event.scheduleCreatorMemberId)
			.isEnabled(NotificationSettingType.FAMILY_SCHEDULE_CHECK)) {
			return
		}

		val confirmerName = circleMemberInPort.getCircleMembers(event.circleId).nicknameOf(event.confirmerMemberId)
		val payload = ScheduleConfirmedByFamilyPayload(confirmerName, event.scheduleTitle)
		val receiverDedupKey = "schedule-confirmed:${event.scheduleId}:${event.confirmerMemberId}:${event.scheduleCreatorMemberId}"
		notificationPushTokenInPort.getActiveReceivable(event.scheduleCreatorMemberId).forEach { pushToken ->
			eventPublisher.publish(
				NotificationRequestedEvent(
					channel = NotificationChannel.PUSH,
					receiver = pushToken.token,
					payload = payload,
					dedupKey = "$receiverDedupKey:token:${requireNotNull(pushToken.id).value}",
				),
			)
		}
	}

	@EventListener
	fun handle(event: ScheduleConfirmationRequestDueEvent) {
		val payload = ScheduleConfirmationRequestedPayload(event.scheduleTitle)
		circleMemberInPort.getCircleMembers(event.circleId)
			.asSequence()
			.filter { it.active && it.memberId != event.scheduleCreatorMemberId }
			.filter { notificationSettingInPort.getSetting(it.memberId).isEnabled(NotificationSettingType.FAMILY_SCHEDULE_CHECK) }
			.filterNot { scheduleConfirmationStatusInPort.hasConfirmed(ScheduleId.of(event.scheduleId), it.memberId) }
			.forEach { member ->
				val receiverDedupKey = "schedule-confirmation-request:${event.scheduleId}:${member.memberId}"
				notificationPushTokenInPort.getActiveReceivable(member.memberId).forEach { pushToken ->
					eventPublisher.publish(
						NotificationRequestedEvent(
							channel = NotificationChannel.PUSH,
							receiver = pushToken.token,
							payload = payload,
							dedupKey = "$receiverDedupKey:token:${requireNotNull(pushToken.id).value}",
						),
					)
				}
			}
	}

	private fun List<CircleMemberDto>.nicknameOf(memberId: String): String =
		firstOrNull { it.memberId == memberId }?.nickname
			?: error("Active circle member not found: memberId=$memberId")

	private fun ScheduleReminderDueEvent.toPayload() = when (reminderType) {
		ScheduleReminderType.D7 -> ScheduleReminderD7Payload(scheduleTitle)
		ScheduleReminderType.D1 -> ScheduleReminderD1Payload(scheduleTitle)
		ScheduleReminderType.DDAY_ALL_DAY -> ScheduleReminderDDayAllDayPayload(scheduleTitle)
		ScheduleReminderType.DDAY_TIMED -> ScheduleReminderDDayTimedPayload(
			scheduleTitle = scheduleTitle,
			startTime = requireNotNull(startTime) { "Timed reminder requires start time" }.format(TIME_FORMATTER),
		)
	}

	private fun ScheduleReminderType.toSettingType(): NotificationSettingType = when (this) {
		ScheduleReminderType.D7 -> NotificationSettingType.D7
		ScheduleReminderType.D1 -> NotificationSettingType.D1
		ScheduleReminderType.DDAY_ALL_DAY,
		ScheduleReminderType.DDAY_TIMED
		-> NotificationSettingType.D_DAY
	}

	companion object {
		private val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
	}
}
