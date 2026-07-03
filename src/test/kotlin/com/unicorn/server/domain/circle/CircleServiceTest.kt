package com.unicorn.server.domain.circle

import com.unicorn.server.TestIdFactory
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.circle.port.dto.CreateCircleCommand
import com.unicorn.server.domain.circle.port.out.CircleIdGenerator
import com.unicorn.server.domain.circle.port.out.CircleMemberIdGenerator
import com.unicorn.server.domain.circle.port.out.CircleMemberOutPort
import com.unicorn.server.domain.circle.port.out.CircleOutPort
import com.unicorn.server.domain.circle.service.CircleService
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.port.dto.MemberProfileDto
import com.unicorn.server.domain.member.port.`in`.GetMemberProfileInPort
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.domain.member.vo.MemberId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("CircleService 단위 테스트")
class CircleServiceTest {
	private val circleOutPort = FakeCircleOutPort()
	private val circleMemberOutPort = FakeCircleMemberOutPort()
	private val memberQueryInPort = FakeMemberQueryInPort()
	private val eventPublisher = RecordingEventPublisher()
	private val circleIdGenerator = object : CircleIdGenerator { override fun next() = TestIdFactory.circleId() }
	private val circleMemberIdGenerator = object : CircleMemberIdGenerator { override fun next() = TestIdFactory.circleMemberId() }
	private val circleService = CircleService(circleOutPort, circleMemberOutPort, circleIdGenerator, circleMemberIdGenerator, memberQueryInPort, eventPublisher)

	@Test
	@DisplayName("써클 생성 시 initiator 멤버십이 함께 생성된다")
	fun create_success() {
		val owner = Member.create(TestIdFactory.memberId(), Email("owner@example.com"), "Owner", "비니", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		memberQueryInPort.save(owner)

		val result = circleService.create(owner.id.toString(), CreateCircleCommand("비니네"))

		assertThat(result.name).isEqualTo("비니네")
		assertThat(circleMemberOutPort.members).hasSize(1)
		assertThat(circleMemberOutPort.members.first().memberId).isEqualTo(owner.id)
	}

	private class FakeCircleOutPort : CircleOutPort {
		private val circles = linkedMapOf<CircleId, Circle>()
		override fun save(circle: Circle): Circle {
			circles[circle.id] = circle
			return circle
		}
		override fun findById(circleId: CircleId): Circle? = circles[circleId]
		override fun findAllByOwnerId(ownerId: MemberId): List<Circle> = circles.values.filter { it.ownerId == ownerId }
	}

	private class FakeCircleMemberOutPort : CircleMemberOutPort {
		val members = mutableListOf<com.unicorn.server.domain.circle.CircleMember>()
		override fun save(circleMember: com.unicorn.server.domain.circle.CircleMember): com.unicorn.server.domain.circle.CircleMember {
			members.removeIf { it.id == circleMember.id }
			members.add(circleMember)
			return circleMember
		}
		override fun findByCircleAndMember(circleId: CircleId, memberId: MemberId) = members.firstOrNull { it.circleId == circleId && it.memberId == memberId }
		override fun findAllActiveByCircleId(circleId: CircleId) = members.filter { it.circleId == circleId }
		override fun existsByCircleAndMember(circleId: CircleId, memberId: MemberId) = members.any { it.circleId == circleId && it.memberId == memberId }
		override fun countActiveByCircleId(circleId: CircleId) = members.count { it.circleId == circleId }.toLong()
	}

	private class FakeMemberQueryInPort : GetMemberProfileInPort {
		private val members = linkedMapOf<MemberId, Member>()
		fun save(member: Member): Member {
			members[member.id] = member
			return member
		}
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
