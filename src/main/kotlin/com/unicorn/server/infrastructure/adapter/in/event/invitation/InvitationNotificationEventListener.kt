package com.unicorn.server.infrastructure.adapter.`in`.event.invitation

import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.invitation.event.InvitationRedeemedEvent
import com.unicorn.server.domain.notification.event.CircleJoinCompletedPayload
import com.unicorn.server.domain.notification.port.`in`.NotificationPublishInPort
import com.unicorn.server.domain.notification.port.dto.PublishNotificationCommand
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class InvitationNotificationEventListener(
	private val circleMemberInPort: CircleMemberInPort,
	private val notificationPublishInPort: NotificationPublishInPort,
) {
	@EventListener
	fun handle(event: InvitationRedeemedEvent) {
		val payload = CircleJoinCompletedPayload(
			memberName = event.redeemerNickname,
			circleName = event.circleName,
		)

		circleMemberInPort.getCircleMembers(event.targetId)
			.asSequence()
			.filter { it.active && it.memberId != event.redeemerMemberId }
			.forEach { member ->
				notificationPublishInPort.publish(
					PublishNotificationCommand(
						receiverMemberId = member.memberId,
						payload = payload,
						eventId = event.invitationId,
						circleId = event.targetId,
					),
				)
			}
	}
}
