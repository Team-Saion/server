package com.unicorn.server.domain.circle.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.circle.Circle
import com.unicorn.server.domain.circle.CircleMember
import com.unicorn.server.domain.circle.enums.CircleMemberStatus
import com.unicorn.server.domain.circle.event.CircleCreatedEvent
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
            val member = getMemberProfileInPort.getMemberProfile(memberId) ?: throw MemberNotFoundException(memberId)
            existingMembership.rejoin(member.nickname)
            circleMemberOutPort.save(existingMembership)
            eventPublisher.publish(CircleMemberJoinedEvent(circleId, memberId, existingMembership.role))
            return JoinCircleResult(circleId = circleId)
        }

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
}
