package com.unicorn.server

import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.circle.vo.CircleMemberId
import com.unicorn.server.domain.invitation.vo.InvitationId
import com.unicorn.server.domain.invitation.vo.InvitationRedemptionId
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.domain.member.vo.SocialAccountId
import java.util.concurrent.atomic.AtomicLong

object TestIdFactory {
	private val sequence = AtomicLong(1)

	fun nextSequence(): Long = sequence.getAndIncrement()

	fun memberId(): MemberId = MemberId.generate(nextSequence())

	fun socialAccountId(): SocialAccountId = SocialAccountId.generate(nextSequence())

	fun circleId(): CircleId = CircleId.generate(nextSequence())

	fun circleMemberId(): CircleMemberId = CircleMemberId.generate(nextSequence())

	fun invitationId(): InvitationId = InvitationId.generate(nextSequence())

	fun invitationRedemptionId(): InvitationRedemptionId = InvitationRedemptionId.generate(nextSequence())
}
