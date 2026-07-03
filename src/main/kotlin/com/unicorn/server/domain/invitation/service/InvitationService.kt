package com.unicorn.server.domain.invitation.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.circle.port.`in`.CircleInPort
import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.invitation.Invitation
import com.unicorn.server.domain.invitation.enums.InvitationStatus
import com.unicorn.server.domain.invitation.enums.InvitationType
import com.unicorn.server.domain.invitation.event.InvitationClickedEvent
import com.unicorn.server.domain.invitation.event.InvitationDispatchedEvent
import com.unicorn.server.domain.invitation.event.InvitationIssuedEvent
import com.unicorn.server.domain.invitation.event.InvitationRedeemedEvent
import com.unicorn.server.domain.invitation.exception.InvitationErrorCode
import com.unicorn.server.domain.invitation.exception.InvitationExpiredException
import com.unicorn.server.domain.invitation.exception.InvitationNotFoundException
import com.unicorn.server.domain.invitation.port.dto.AcceptResult
import com.unicorn.server.domain.invitation.port.dto.DispatchInvitationCommand
import com.unicorn.server.domain.invitation.port.dto.InvitationDetailView
import com.unicorn.server.domain.invitation.port.dto.IssueInvitationCommand
import com.unicorn.server.domain.invitation.port.dto.IssuedInvitationResult
import com.unicorn.server.domain.invitation.port.`in`.AcceptCircleInvitationInPort
import com.unicorn.server.domain.invitation.port.`in`.DispatchInvitationInPort
import com.unicorn.server.domain.invitation.port.`in`.GetInvitationByTokenInPort
import com.unicorn.server.domain.invitation.port.`in`.IssueInvitationInPort
import com.unicorn.server.domain.invitation.port.out.InvitationOutPort
import com.unicorn.server.domain.invitation.port.out.InvitationIdGenerator
import com.unicorn.server.domain.invitation.port.out.InvitationTokenGenerator
import com.unicorn.server.domain.invitation.vo.InviteMessage
import com.unicorn.server.domain.invitation.vo.InviteToName
import com.unicorn.server.domain.invitation.vo.InvitationId
import com.unicorn.server.domain.invitation.vo.InvitationToken
import com.unicorn.server.domain.member.exception.MemberNotFoundException
import com.unicorn.server.domain.member.port.`in`.GetMemberProfileInPort
import com.unicorn.server.domain.member.vo.MemberId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class InvitationService(
	private val invitationOutPort: InvitationOutPort,
	private val invitationIdGenerator: InvitationIdGenerator,
	private val tokenGenerator: InvitationTokenGenerator,
	private val circleInPort: CircleInPort,
	private val circleMemberInPort: CircleMemberInPort,
	private val getMemberProfileInPort: GetMemberProfileInPort,
	private val eventPublisher: EventPublisher,
) : IssueInvitationInPort, DispatchInvitationInPort, GetInvitationByTokenInPort, AcceptCircleInvitationInPort {
	override fun issue(inviterMemberId: String, command: IssueInvitationCommand): IssuedInvitationResult {
		if (command.type != InvitationType.CIRCLE) {
			throw BusinessException(InvitationErrorCode.INVITATION_TARGET_INVALID)
		}
		circleInPort.getCircleSummary(command.targetId)
		if (!circleMemberInPort.isCircleMember(command.targetId, inviterMemberId)) {
			throw BusinessException(InvitationErrorCode.INVITATION_NOT_AUTHORIZED)
		}
		val inviterId = MemberId.of(inviterMemberId)

		val invitation = invitationOutPort.save(
			Invitation.create(
				id = invitationIdGenerator.next(),
				type = command.type,
				targetId = command.targetId,
				token = tokenGenerator.generate(),
				inviterId = inviterId,
				inviteToName = command.inviteToName?.takeIf { it.isNotBlank() }?.let(::InviteToName),
				message = command.message?.takeIf { it.isNotBlank() }?.let(::InviteMessage),
			),
		)
		eventPublisher.publish(InvitationIssuedEvent(invitation.id.toString(), invitation.type, invitation.targetId, invitation.inviterId.toString()))
		return IssuedInvitationResult(
			invitationId = invitation.id.toString(),
			token = invitation.token.value,
			expiresAt = invitation.expiresAt,
		)
	}

	override fun dispatch(command: DispatchInvitationCommand) {
		val invitation = invitationOutPort.findById(InvitationId.of(command.invitationId))
			?: throw InvitationNotFoundException(command.invitationId)
		eventPublisher.publish(InvitationDispatchedEvent(invitation.id.toString(), command.channel))
	}

	override fun getByToken(token: String): InvitationDetailView {
		val invitation = invitationOutPort.findByToken(InvitationToken(token)) ?: throw InvitationNotFoundException(token)
		val now = LocalDateTime.now()
		if (invitation.status == InvitationStatus.ACTIVE && invitation.isExpired(now)) {
			invitation.markExpired()
			invitationOutPort.save(invitation)
		}
		if (!invitation.isUsable(now)) {
			throw InvitationExpiredException(invitation.id.toString())
		}
		val inviter = getMemberProfileInPort.getMemberProfile(invitation.inviterId.toString()) ?: throw MemberNotFoundException(invitation.inviterId.toString())
		val circle = circleInPort.getCircleSummary(invitation.targetId)
		eventPublisher.publish(InvitationClickedEvent(invitation.id.toString()))
		return InvitationDetailView(
			invitationId = invitation.id.toString(),
			circleName = circle.name,
			inviterNickname = inviter.nickname,
			inviterAvatarColor = inviter.avatarColor,
			expiresAt = invitation.expiresAt,
		)
	}

	override fun accept(token: String, memberId: String): AcceptResult {
		val invitation = invitationOutPort.findByToken(InvitationToken(token)) ?: throw InvitationNotFoundException(token)
		val now = LocalDateTime.now()
		if (invitation.status == InvitationStatus.ACTIVE && invitation.isExpired(now)) {
			invitation.markExpired()
			invitationOutPort.save(invitation)
		}
		if (!invitation.isUsable(now)) {
			throw InvitationExpiredException(invitation.id.toString())
		}
		if (invitation.type != InvitationType.CIRCLE) {
			throw BusinessException(InvitationErrorCode.INVITATION_TARGET_INVALID)
		}
		val redeemerMemberId = MemberId.of(memberId)
		invitation.ensureNotSelfApproval(redeemerMemberId)

		val joinResult = circleMemberInPort.join(invitation.targetId, memberId)
		eventPublisher.publish(
			InvitationRedeemedEvent(
				invitationId = invitation.id.toString(),
				type = invitation.type,
				targetId = invitation.targetId,
				redeemerMemberId = memberId,
			),
		)
		return AcceptResult(joinResult.circleId)
	}
}
