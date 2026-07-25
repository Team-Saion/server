package com.unicorn.server.domain.circle.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.circle.Circle
import com.unicorn.server.domain.circle.CircleMember
import com.unicorn.server.domain.circle.enums.CircleMemberStatus
import com.unicorn.server.domain.circle.enums.CircleRole
import com.unicorn.server.domain.circle.event.CircleInitiatorTransferredEvent
import com.unicorn.server.domain.circle.event.CircleMemberJoinedEvent
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.domain.circle.exception.CircleNotFoundException
import com.unicorn.server.domain.circle.exception.CircleSuccessorNotFoundException
import com.unicorn.server.domain.circle.port.dto.CircleMemberDto
import com.unicorn.server.domain.circle.port.dto.CircleSummary
import com.unicorn.server.domain.circle.port.dto.JoinCircleResult
import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
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
class CircleMemberService(
    private val circleOutPort: CircleOutPort,
    private val circleMemberOutPort: CircleMemberOutPort,
    private val circleMemberIdGenerator: CircleMemberIdGenerator,
    private val getMemberProfileInPort: GetMemberProfileInPort,
    private val eventPublisher: EventPublisher,
) : CircleMemberInPort {
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

    override fun leave(circleId: String, memberId: String) {
        val targetCircleId = CircleId.of(circleId)
        val targetMemberId = MemberId.of(memberId)
        val circle = circleOutPort.findById(targetCircleId)
            ?: throw CircleNotFoundException(circleId)
        if (circle.deleted) {
            throw CircleNotFoundException(circleId)
        }

        val membership = circleMemberOutPort.findByCircleAndMember(targetCircleId, targetMemberId)
            ?: throw BusinessException(CircleErrorCode.CIRCLE_ACCESS_DENIED)
        if (membership.status != CircleMemberStatus.ACTIVE || membership.deleted) {
            throw BusinessException(CircleErrorCode.CIRCLE_ACCESS_DENIED)
        }

        leaveCircleMember(circle, membership, targetMemberId)
    }

    override fun getCircleMembers(circleId: String): List<CircleMemberDto> =
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

    override fun transferInitiator(
        circleId: String,
        currentInitiatorId: String,
        newInitiatorId: String,
    ): CircleSummary {
        val targetCircleId = CircleId.of(circleId)
        val requesterId = MemberId.of(currentInitiatorId)
        val targetId = MemberId.of(newInitiatorId)
        if (requesterId == targetId) {
            throw BusinessException(CircleErrorCode.INITIATOR_DELEGATION_SELF_FORBIDDEN)
        }

        val circle = circleOutPort.findById(targetCircleId) ?: throw CircleNotFoundException(circleId)
        val requesterMembership = circleMemberOutPort.findByCircleAndMember(targetCircleId, requesterId)
            ?: throw BusinessException(CircleErrorCode.INITIATOR_DELEGATION_FORBIDDEN)
        val targetMembership = circleMemberOutPort.findByCircleAndMember(targetCircleId, targetId)
            ?: throw BusinessException(CircleErrorCode.INITIATOR_DELEGATION_TARGET_INVALID)

        circle.transferInitiator(requesterMembership, targetMembership)
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
            val circle = circleOutPort.findById(membership.circleId)
                ?: throw CircleNotFoundException(membership.circleId.toString())
            leaveCircleMember(circle, membership, withdrawingMemberId)
        }
    }

    private fun leaveCircleMember(
        circle: Circle,
        membership: CircleMember,
        departingMemberId: MemberId,
    ) {
        if (membership.role != CircleRole.INITIATOR) {
            circle.leaveMember(membership)
            circleMemberOutPort.save(membership)
            return
        }

        if (!circleMemberOutPort.existsActiveMemberByCircleIdExcludingMemberId(circle.id, departingMemberId)) {
            circle.deleteWithLastMember(membership)
            circleMemberOutPort.save(membership)
            circleOutPort.save(circle)
            return
        }

        val successor = try {
            circleMemberOutPort.findOldestActiveByCircleIdExcludingMemberId(circle.id, departingMemberId)
        } catch (_: CircleSuccessorNotFoundException) {
            circle.deleteWithLastMember(membership)
            circleMemberOutPort.save(membership)
            circleOutPort.save(circle)
            return
        }

        val newInitiator = circle.leaveMember(membership, successor)
        circleMemberOutPort.save(membership)
        circleMemberOutPort.save(newInitiator)
        val savedCircle = circleOutPort.save(circle)
        eventPublisher.publish(
            CircleInitiatorTransferredEvent(
                circleId = savedCircle.id.toString(),
                previousInitiatorMemberId = departingMemberId.toString(),
                newInitiatorMemberId = newInitiator.memberId.toString(),
            ),
        )
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
