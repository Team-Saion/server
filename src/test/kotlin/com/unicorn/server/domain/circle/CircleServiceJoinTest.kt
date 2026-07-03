package com.unicorn.server.domain.circle

import com.unicorn.server.TestIdFactory
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.circle.port.out.CircleMemberOutPort
import com.unicorn.server.domain.circle.port.out.CircleIdGenerator
import com.unicorn.server.domain.circle.port.out.CircleMemberIdGenerator
import com.unicorn.server.domain.circle.port.out.CircleOutPort
import com.unicorn.server.domain.circle.service.CircleService
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.port.dto.MemberProfileDto
import com.unicorn.server.domain.member.port.`in`.GetMemberProfileInPort
import com.unicorn.server.domain.member.vo.MemberId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CircleService join 단위 테스트")
class CircleServiceJoinTest {
	private val circleOutPort = FakeCircleOutPort()
	private val circleMemberOutPort = FakeCircleMemberOutPort()
	private val memberQueryInPort = FakeMemberQueryInPort()
	private val eventPublisher = RecordingEventPublisher()
	private val circleIdGenerator = object : CircleIdGenerator { override fun next() = TestIdFactory.circleId() }
	private val circleMemberIdGenerator = object : CircleMemberIdGenerator { override fun next() = TestIdFactory.circleMemberId() }
	private val circleService = CircleService(circleOutPort, circleMemberOutPort, circleIdGenerator, circleMemberIdGenerator, memberQueryInPort, eventPublisher)

	@Test
	@DisplayName("이미 참여한 사용자가 join 하면 예외를 던진다")
	fun join_alreadyJoined_throwsException() {
		val owner = Member.create(TestIdFactory.memberId(), Email("owner2@example.com"), "Owner", "owner2", role = Role.MEMBER)
		val friend = Member.create(TestIdFactory.memberId(), Email("friend@example.com"), "Friend", "friend", role = Role.MEMBER)
		memberQueryInPort.save(owner)
		memberQueryInPort.save(friend)

		val circle = Circle.create(TestIdFactory.circleId(), "테스트써클1", owner.id)
		circleOutPort.save(circle)
		circleMemberOutPort.save(CircleMember.createMember(TestIdFactory.circleMemberId(), circle.id, friend.id, friend.nickname))

		org.junit.jupiter.api.assertThrows<com.unicorn.server.common.exception.BusinessException> {
			circleService.join(circle.id.toString(), friend.id.toString())
		}
		assertThat(circleMemberOutPort.members).hasSize(1)
	}

	private class FakeCircleOutPort : CircleOutPort {
		private val circles = linkedMapOf<CircleId, Circle>()
		override fun save(circle: Circle): Circle { circles[circle.id] = circle; return circle }
		override fun findById(circleId: CircleId): Circle? = circles[circleId]
		override fun findAllByOwnerId(ownerId: MemberId): List<Circle> = circles.values.filter { it.ownerId == ownerId }
	}

	private class FakeCircleMemberOutPort : CircleMemberOutPort {
		val members = mutableListOf<CircleMember>()
		override fun save(circleMember: CircleMember): CircleMember { members.removeIf { it.id == circleMember.id }; members.add(circleMember); return circleMember }
		override fun findByCircleAndMember(circleId: CircleId, memberId: MemberId): CircleMember? = members.firstOrNull { it.circleId == circleId && it.memberId == memberId }
		override fun findAllActiveByCircleId(circleId: CircleId): List<CircleMember> = members.filter { it.circleId == circleId }
		override fun findAllActiveByMemberId(memberId: MemberId): List<CircleMember> = members.filter { it.memberId == memberId }
		override fun existsByCircleAndMember(circleId: CircleId, memberId: MemberId): Boolean = members.any { it.circleId == circleId && it.memberId == memberId }
		override fun countActiveByCircleId(circleId: CircleId): Long = members.count { it.circleId == circleId }.toLong()
	}

	private class FakeMemberQueryInPort : GetMemberProfileInPort {
		private val members = linkedMapOf<MemberId, Member>()
		fun save(member: Member): Member { members[member.id] = member; return member }
		override fun getMemberProfile(memberId: String): MemberProfileDto? =
			members[MemberId.of(memberId)]?.let {
				MemberProfileDto(
					memberId = it.id.toString(),
					nickname = it.nickname,
					avatarColor = it.avatarColor.name,
					kakaoNickname = null,
					active = !it.isDeleted(),
				)
			}
	}

	private class RecordingEventPublisher : EventPublisher {
		override fun publish(event: com.unicorn.server.common.domain.Event) = Unit
	}
}
