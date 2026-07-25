package com.unicorn.server.domain.circle.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.circle.Circle
import com.unicorn.server.domain.circle.CircleMember
import com.unicorn.server.domain.circle.event.CircleCreatedEvent
import com.unicorn.server.domain.circle.event.CircleMemberJoinedEvent
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.domain.circle.exception.CircleNotFoundException
import com.unicorn.server.domain.circle.port.dto.CircleSummary
import com.unicorn.server.domain.circle.port.dto.CreateCircleCommand
import com.unicorn.server.domain.circle.port.`in`.CircleInPort
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
) : CircleInPort {
    override fun create(memberId: String, command: CreateCircleCommand): CircleSummary {
        val owner = getMemberProfileInPort.getMemberProfile(memberId) ?: throw MemberNotFoundException(memberId)
        val ownerId = MemberId.of(owner.memberId)
        assertNoActiveCircle(ownerId)
        val circle = circleOutPort.save(Circle.create(circleIdGenerator.next(), command.name, ownerId))
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
            .sortedWith(
                compareByDescending<CircleMember> { it.joinedAt }
                    .thenByDescending { it.id.toString() },
            )
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

    private fun assertNoActiveCircle(memberId: MemberId) {
        if (circleMemberOutPort.findAllActiveByMemberId(memberId).isNotEmpty()) {
            throw BusinessException(CircleErrorCode.ALREADY_HAS_ACTIVE_CIRCLE)
        }
    }
}
