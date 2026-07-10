package com.unicorn.server.domain.circle.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.circle.Circle
import com.unicorn.server.domain.circle.CircleMember
import com.unicorn.server.domain.circle.enums.CircleMemberStatus
import com.unicorn.server.domain.circle.enums.CircleRole
import com.unicorn.server.domain.circle.event.CircleCreatedEvent
import com.unicorn.server.domain.circle.event.CircleInitiatorTransferredEvent
import com.unicorn.server.domain.circle.event.CircleMemberJoinedEvent
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.domain.circle.exception.CircleNotFoundException
import com.unicorn.server.domain.circle.port.dto.CircleMemberDto
import com.unicorn.server.domain.circle.port.dto.CircleSummary
import com.unicorn.server.domain.circle.port.dto.CreateCircleCommand
import com.unicorn.server.domain.circle.port.dto.JoinCircleResult
import com.unicorn.server.domain.circle.port.`in`.CircleInPort
import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.circle.port.out.CircleIdGenerator
import com.unicorn.server.domain.circle.port.out.CircleMemberIdGenerator
import com.unicorn.server.domain.circle.port.out.CircleMemberOutPort
import com.unicorn.server.domain.circle.port.out.CircleOutPort
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.member.exception.MemberNotFoundException
import com.unicorn.server.domain.member.port.`in`.GetMemberProfileInPort
import com.unicorn.server.domain.member.vo.MemberId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CircleService(
    private val circleOutPort: CircleOutPort,
    private val circleMemberOutPort: CircleMemberOutPort,
    private val circleIdGenerator: CircleIdGenerator,
    private val circleMemberIdGenerator: CircleMemberIdGenerator,
	private val getMemberProfileInPort: GetMemberProfileInPort,
	private val eventPublisher: EventPublisher,
) : CircleInPort, CircleMemberInPort {
	override fun create(memberId: String, command: CreateCircleCommand): CircleSummary {
		val owner = getMemberProfileInPort.getMemberProfile(memberId) ?: throw MemberNotFoundException(memberId)
		val ownerId = MemberId.of(owner.memberId)
		assertNoActiveCircle(ownerId)
		val circleId = circleIdGenerator.next()
		val circle = circleOutPort.save(Circle.create(circleId, command.name, ownerId))
        val circleMember = circleMemberOutPort.save(
            CircleMember.createInitiator(circleMemberIdGenerator.next(), circle.id, ownerId, owner.nickname),
        )
        eventPublisher.publish(CircleCreatedEvent(circle.id.toString(), ownerId.toString()))
        eventPublisher.publish(
            CircleMemberJoinedEvent(
                circle.id.toString(),
                circleMember.memberId.toString(),
                circleMember.role
            )
        )
        return CircleSummary(circle.id.toString(), circle.name, circle.ownerId.toString())
    }

    override fun listCircles(memberId: String): List<CircleSummary> {
        val memberships = circleMemberOutPort.findAllActiveByMemberId(MemberId.of(memberId))
            .sortedByDescending { it.joinedAt }
            .distinctBy { it.circleId }
        val circleIds = memberships.map { it.circleId }
        val circles = circleOutPort.findAllByIds(circleIds)

        return memberships.mapNotNull { membership ->
            circles[membership.circleId]?.takeIf { !it.deleted }
                ?.let { circle -> CircleSummary(circle.id.toString(), circle.name, circle.ownerId.toString()) }
        }
    }

    override fun getCircleSummary(circleId: String): CircleSummary {
        val circle = circleOutPort.findById(CircleId.of(circleId)) ?: throw CircleNotFoundException(circleId)
        return CircleSummary(circle.id.toString(), circle.name, circle.ownerId.toString())
    }

	override fun join(circleId: String, memberId: String): JoinCircleResult {
		val targetCircleId = CircleId.of(circleId)
		val targetMemberId = MemberId.of(memberId)
		circleOutPort.findById(targetCircleId) ?: throw CircleNotFoundException(circleId)

		val existingMembership = circleMemberOutPort.findByCircleAndMember(targetCircleId, targetMemberId)
		if (existingMembership != null) {
			if (existingMembership.status == CircleMemberStatus.ACTIVE && !existingMembership.deleted) {
				throw BusinessException(CircleErrorCode.ALREADY_JOINED)
			}
			assertNoActiveCircle(targetMemberId)
			assertCircleCapacity(targetCircleId)
			val member = getMemberProfileInPort.getMemberProfile(memberId) ?: throw MemberNotFoundException(memberId)
			existingMembership.rejoin(member.nickname)
			circleMemberOutPort.save(existingMembership)
            eventPublisher.publish(CircleMemberJoinedEvent(circleId, memberId, existingMembership.role))
            return JoinCircleResult(circleId = circleId)
		}

		assertNoActiveCircle(targetMemberId)
		assertCircleCapacity(targetCircleId)
		val member = getMemberProfileInPort.getMemberProfile(memberId) ?: throw MemberNotFoundException(memberId)
		val circleMember = circleMemberOutPort.save(
			CircleMember.createMember(circleMemberIdGenerator.next(), targetCircleId, targetMemberId, member.nickname),
        )
        eventPublisher.publish(CircleMemberJoinedEvent(circleId, memberId, circleMember.role))
        return JoinCircleResult(circleId = circleId)
    }

    override fun getCircleMembers(circleId: String) =
        circleMemberOutPort.findAllActiveByCircleId(CircleId.of(circleId))
            .filter { it.status == CircleMemberStatus.ACTIVE && !it.deleted }
            .map {
                CircleMemberDto(
                    memberId = it.memberId.toString(),
                    nickname = it.nickname,
                    role = it.role.name,
                    active = true,
                )
            }

	override fun isCircleMember(circleId: String, memberId: String): Boolean =
		circleMemberOutPort.existsActiveByCircleAndMember(CircleId.of(circleId), MemberId.of(memberId))

	override fun transferInitiator(circleId: String, currentInitiatorId: String, newInitiatorId: String): CircleSummary {
		val targetCircleId = CircleId.of(circleId)
		val requesterId = MemberId.of(currentInitiatorId)
		val targetId = MemberId.of(newInitiatorId)
		if (requesterId == targetId) {
			throw BusinessException(CircleErrorCode.INITIATOR_DELEGATION_SELF_FORBIDDEN)
		}

		val circle = circleOutPort.findById(targetCircleId) ?: throw CircleNotFoundException(circleId)
		val requesterMembership = circleMemberOutPort.findByCircleAndMember(targetCircleId, requesterId)
			?: throw BusinessException(CircleErrorCode.INITIATOR_DELEGATION_FORBIDDEN)
		if (requesterMembership.status != CircleMemberStatus.ACTIVE || requesterMembership.deleted || requesterMembership.role != com.unicorn.server.domain.circle.enums.CircleRole.INITIATOR) {
			throw BusinessException(CircleErrorCode.INITIATOR_DELEGATION_FORBIDDEN)
		}

		val targetMembership = circleMemberOutPort.findByCircleAndMember(targetCircleId, targetId)
			?: throw BusinessException(CircleErrorCode.INITIATOR_DELEGATION_TARGET_INVALID)
		if (targetMembership.status != CircleMemberStatus.ACTIVE || targetMembership.deleted || targetMembership.role != com.unicorn.server.domain.circle.enums.CircleRole.MEMBER) {
			throw BusinessException(CircleErrorCode.INITIATOR_DELEGATION_TARGET_INVALID)
		}

		requesterMembership.demoteToMember()
		targetMembership.promoteToInitiator()
		circle.transferOwner(targetId)
		circleMemberOutPort.save(requesterMembership)
		circleMemberOutPort.save(targetMembership)
		val savedCircle = circleOutPort.save(circle)
		eventPublisher.publish(
			CircleInitiatorTransferredEvent(
				circleId = savedCircle.id.toString(),
				previousInitiatorMemberId = requesterId.toString(),
				newInitiatorMemberId = targetId.toString(),
			),
		)
		return CircleSummary(savedCircle.id.toString(), savedCircle.name, savedCircle.ownerId.toString())
	}

	override fun handleMemberWithdrawal(memberId: String) {
		val withdrawingMemberId = MemberId.of(memberId)
		val memberships = circleMemberOutPort.findAllActiveByMemberId(withdrawingMemberId)

		memberships.forEach { membership ->
			val circle = circleOutPort.findById(membership.circleId) ?: throw CircleNotFoundException(membership.circleId.toString())
			val remainingActiveMembers = circleMemberOutPort.findAllActiveByCircleId(membership.circleId)
				.filter { it.memberId != withdrawingMemberId }

			if (membership.role == CircleRole.INITIATOR) {
				if (remainingActiveMembers.isEmpty()) {
					circle.softDelete()
					circleOutPort.save(circle)
				} else {
					val newInitiator = remainingActiveMembers.minWith(compareBy<CircleMember>({ it.joinedAt }, { it.memberId.toString() }))
					membership.demoteToMember()
					newInitiator.promoteToInitiator()
					circle.transferOwner(newInitiator.memberId)
					circleMemberOutPort.save(newInitiator)
					val savedCircle = circleOutPort.save(circle)
					eventPublisher.publish(
						CircleInitiatorTransferredEvent(
							circleId = savedCircle.id.toString(),
							previousInitiatorMemberId = withdrawingMemberId.toString(),
							newInitiatorMemberId = newInitiator.memberId.toString(),
						),
					)
				}
			}

			membership.leaveByWithdrawal()
			circleMemberOutPort.save(membership)
		}
	}

	private fun assertNoActiveCircle(memberId: MemberId) {
		if (circleMemberOutPort.findAllActiveByMemberId(memberId).isNotEmpty()) {
			throw BusinessException(CircleErrorCode.ALREADY_HAS_ACTIVE_CIRCLE)
		}
	}

	private fun assertCircleCapacity(circleId: CircleId) {
		if (circleMemberOutPort.countActiveByCircleId(circleId) >= MAX_MEMBERS) {
			throw BusinessException(CircleErrorCode.CIRCLE_MEMBER_LIMIT_EXCEEDED)
		}
	}

	companion object {
		private const val MAX_MEMBERS = 10L
	}
}
