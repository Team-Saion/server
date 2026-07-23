package com.unicorn.server.infrastructure.adapter.`in`.event.schedule

import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.circle.port.dto.CircleMemberDto
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.event.NotificationRequestedEvent
import com.unicorn.server.domain.notification.event.ScheduleCreatedPayload
import com.unicorn.server.domain.notification.port.`in`.NotificationPushTokenInPort
import com.unicorn.server.domain.schedule.event.ScheduleCreatedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ScheduleNotificationEventListener(
	private val circleMemberInPort: CircleMemberInPort,
	private val notificationPushTokenInPort: NotificationPushTokenInPort,
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

	private fun List<CircleMemberDto>.nicknameOf(memberId: String): String =
		firstOrNull { it.memberId == memberId }?.nickname
			?: error("Active circle member not found: memberId=$memberId")
}
