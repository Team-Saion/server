package com.unicorn.server.infrastructure.adapter.`in`.event.invitation

import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.invitation.event.InvitationRedeemedEvent
import com.unicorn.server.domain.notification.event.CircleJoinCompletedPayload
import com.unicorn.server.domain.notification.port.`in`.NotificationRequestInPort
import com.unicorn.server.domain.notification.port.dto.RequestNotificationCommand
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class InvitationNotificationEventListener(
	private val circleMemberInPort: CircleMemberInPort,
	private val notificationRequestInPort: NotificationRequestInPort,
) {
	@EventListener
	fun handle(event: InvitationRedeemedEvent) {
		log.debug(
			"[CircleJoinNotification] InvitationRedeemedEvent received: invitationId={}, circleId={}, redeemerMemberId={}",
			event.invitationId,
			event.targetId,
			event.redeemerMemberId,
		)
		val payload = CircleJoinCompletedPayload(
			memberName = event.redeemerNickname,
			circleName = event.circleName,
		)

		val circleMembers = circleMemberInPort.getCircleMembers(event.targetId)
		val receivers = circleMembers
			.asSequence()
			.filter { it.active && it.memberId != event.redeemerMemberId }
			.toList()
		log.debug(
			"[CircleJoinNotification] receivers selected: invitationId={}, circleId={}, totalMembers={}, receiverCount={}, receiverMemberIds={}",
			event.invitationId,
			event.targetId,
			circleMembers.size,
			receivers.size,
			receivers.map { it.memberId },
		)

		receivers.forEach { member ->
			log.debug(
				"[CircleJoinNotification] requesting notification: invitationId={}, receiverMemberId={}",
				event.invitationId,
				member.memberId,
			)
			notificationRequestInPort.request(
				RequestNotificationCommand(
					receiverMemberId = member.memberId,
					payload = payload,
					eventId = event.invitationId,
					circleId = event.targetId,
				),
			)
			log.debug(
				"[CircleJoinNotification] notification request completed: invitationId={}, receiverMemberId={}",
				event.invitationId,
				member.memberId,
			)
		}
	}

	companion object {
		private val log = LoggerFactory.getLogger(InvitationNotificationEventListener::class.java)
	}
}
