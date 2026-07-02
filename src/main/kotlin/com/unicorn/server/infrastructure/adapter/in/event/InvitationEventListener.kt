package com.unicorn.server.infrastructure.adapter.`in`.event

import com.unicorn.server.domain.invitation.InvitationRedemption
import com.unicorn.server.domain.invitation.event.InvitationClickedEvent
import com.unicorn.server.domain.invitation.event.InvitationDispatchedEvent
import com.unicorn.server.domain.invitation.event.InvitationRedeemedEvent
import com.unicorn.server.domain.invitation.port.out.InvitationClickLogOutPort
import com.unicorn.server.domain.invitation.port.out.InvitationDispatchLogOutPort
import com.unicorn.server.domain.invitation.port.out.InvitationRedemptionLogOutPort
import com.unicorn.server.domain.invitation.vo.InvitationId
import com.unicorn.server.domain.member.vo.MemberId
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class InvitationEventListener(
	private val invitationDispatchLogOutPort: InvitationDispatchLogOutPort,
	private val invitationClickLogOutPort: InvitationClickLogOutPort,
	private val invitationRedemptionLogOutPort: InvitationRedemptionLogOutPort,
) {
	@EventListener
	fun handleInvitationDispatched(event: InvitationDispatchedEvent) {
		log.info("Invitation dispatched - invitationId: {}, channel: {}", event.invitationId, event.channel)
		invitationDispatchLogOutPort.save(InvitationId.of(event.invitationId), event.channel, LocalDateTime.now())
	}

	@EventListener
	fun handleInvitationClicked(event: InvitationClickedEvent) {
		log.info("Invitation clicked - invitationId: {}", event.invitationId)
		invitationClickLogOutPort.save(InvitationId.of(event.invitationId), LocalDateTime.now())
	}

	@EventListener
	fun handleInvitationRedeemed(event: InvitationRedeemedEvent) {
		log.info("Invitation redeemed - invitationId: {}, redeemerMemberId: {}", event.invitationId, event.redeemerMemberId)
		val now = LocalDateTime.now()
		invitationRedemptionLogOutPort.save(
			InvitationRedemption.create(
				invitationId = InvitationId.of(event.invitationId),
				redeemerMemberId = MemberId.of(event.redeemerMemberId),
				now = now,
			),
		)
	}

	companion object {
		private val log = LoggerFactory.getLogger(InvitationEventListener::class.java)
	}
}
