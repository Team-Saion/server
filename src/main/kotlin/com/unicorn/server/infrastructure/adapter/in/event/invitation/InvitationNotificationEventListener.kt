package com.unicorn.server.infrastructure.adapter.`in`.event.invitation

import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.invitation.event.InvitationRedeemedEvent
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.event.CircleJoinCompletedPayload
import com.unicorn.server.domain.notification.event.NotificationRequestedEvent
import com.unicorn.server.domain.notification.port.`in`.NotificationPushTokenInPort
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class InvitationNotificationEventListener(
	private val circleMemberInPort: CircleMemberInPort,
	private val notificationPushTokenInPort: NotificationPushTokenInPort,
	private val eventPublisher: EventPublisher,
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
			.flatMap { notificationPushTokenInPort.getActiveReceivable(it.memberId).asSequence() }
			.forEach { pushToken ->
				eventPublisher.publish(
					NotificationRequestedEvent(
						channel = NotificationChannel.PUSH,
						receiver = pushToken.token,
						payload = payload,
						dedupKey = "circle-join-completed:${event.invitationId}:${requireNotNull(pushToken.id).value}",
					),
				)
			}
	}
}
