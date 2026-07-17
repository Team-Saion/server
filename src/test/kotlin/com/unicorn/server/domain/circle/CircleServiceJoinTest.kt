package com.unicorn.server.domain.circle

import com.unicorn.server.TestIdFactory
import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.circle.enums.CircleMemberStatus
import com.unicorn.server.domain.circle.exception.CircleErrorCode
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
import org.assertj.core.api.Assertions.assertThatThrownBy
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
		val owner = Member.create(Email("owner2@example.com"), "Owner", "owner2", role = Role.MEMBER)
		val friend = Member.create(Email("friend@example.com"), "Friend", "friend", role = Role.MEMBER)
		memberQueryInPort.save(owner)
		memberQueryInPort.save(friend)

		val circle = Circle.create(TestIdFactory.circleId(), "테스트써클1", owner.id)
		circleOutPort.save(circle)
		circleMemberOutPort.save(CircleMember.createMember(TestIdFactory.circleMemberId(), circle.id, friend.id, friend.nickname))

		assertThatThrownBy { circleService.join(circle.id.toString(), friend.id.toString()) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.ALREADY_JOINED)
		assertThat(circleMemberOutPort.members).hasSize(1)
	}

	@Test
	@DisplayName("탈퇴한 사용자가 재가입하면 기존 멤버십이 ACTIVE로 복구된다")
	fun join_leftMember_rejoins() {
		val owner = Member.create(Email("owner3@example.com"), "Owner", "owner3", role = Role.MEMBER)
		val friend = Member.create(Email("friend2@example.com"), "Friend", "friend2", role = Role.MEMBER)
		memberQueryInPort.save(owner)
		memberQueryInPort.save(friend)

		val circle = Circle.create(TestIdFactory.circleId(), "재가입테스트써클", owner.id)
		circleOutPort.save(circle)
		val membership = CircleMember.createMember(TestIdFactory.circleMemberId(), circle.id, friend.id, friend.nickname)
		membership.leave()
		circleMemberOutPort.save(membership)

		val result = circleService.join(circle.id.toString(), friend.id.toString())

		assertThat(result.circleId).isEqualTo(circle.id.toString())
		assertThat(circleMemberOutPort.members).hasSize(1)
		val rejoinedMember = circleMemberOutPort.members.first()
		assertThat(rejoinedMember.status).isEqualTo(CircleMemberStatus.ACTIVE)
		assertThat(rejoinedMember.leftAt).isNull()
	}

	@Test
	@DisplayName("현재 활성 써클이 있으면 다른 써클에 참여할 수 없다")
	fun join_withAnotherActiveCircle_throwsException() {
		val owner = Member.create(Email("owner5@example.com"), "Owner", "owner5", role = Role.MEMBER)
		val friend = Member.create(Email("friend4@example.com"), "Friend", "friend4", role = Role.MEMBER)
		memberQueryInPort.save(owner)
		memberQueryInPort.save(friend)

		val joinedCircle = Circle.create(TestIdFactory.circleId(), "참여중써클", owner.id)
		val targetCircle = Circle.create(TestIdFactory.circleId(), "대상써클", owner.id)
		circleOutPort.save(joinedCircle)
		circleOutPort.save(targetCircle)
		circleMemberOutPort.save(CircleMember.createMember(TestIdFactory.circleMemberId(), joinedCircle.id, friend.id, friend.nickname))

		assertThatThrownBy { circleService.join(targetCircle.id.toString(), friend.id.toString()) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.ALREADY_HAS_ACTIVE_CIRCLE)
	}

	@Test
	@DisplayName("구성원이 10명인 써클에는 추가로 참여할 수 없다")
	fun join_withFullCircle_throwsException() {
		val owner = Member.create(Email("owner6@example.com"), "Owner", "owner6", role = Role.MEMBER)
		val friend = Member.create(Email("friend5@example.com"), "Friend", "friend5", role = Role.MEMBER)
		memberQueryInPort.save(owner)
		memberQueryInPort.save(friend)

		val circle = Circle.create(TestIdFactory.circleId(), "만원써클", owner.id)
		circleOutPort.save(circle)
		repeat(10) { index ->
			val member = Member.create(Email("full$index@example.com"), "Full$index", "full$index", role = Role.MEMBER)
			memberQueryInPort.save(member)
			circleMemberOutPort.save(CircleMember.createMember(TestIdFactory.circleMemberId(), circle.id, member.id, member.nickname))
		}

		assertThatThrownBy { circleService.join(circle.id.toString(), friend.id.toString()) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.CIRCLE_MEMBER_LIMIT_EXCEEDED)
	}

	@Test
	@DisplayName("탈퇴한 사용자는 isCircleMember에서 false를 반환한다")
	fun isCircleMember_leftMember_returnsFalse() {
		val owner = Member.create(Email("owner4@example.com"), "Owner", "owner4", role = Role.MEMBER)
		val friend = Member.create(Email("friend3@example.com"), "Friend", "friend3", role = Role.MEMBER)
		memberQueryInPort.save(owner)
		memberQueryInPort.save(friend)

		val circle = Circle.create(TestIdFactory.circleId(), "탈퇴테스트써클", owner.id)
		circleOutPort.save(circle)
		val membership = CircleMember.createMember(TestIdFactory.circleMemberId(), circle.id, friend.id, friend.nickname)
		membership.leave()
		circleMemberOutPort.save(membership)

		val result = circleService.isCircleMember(circle.id.toString(), friend.id.toString())

		assertThat(result).isFalse()
	}

	private class FakeCircleOutPort : CircleOutPort {
		private val circles = linkedMapOf<CircleId, Circle>()
		override fun save(circle: Circle): Circle { circles[circle.id] = circle; return circle }
		override fun findById(circleId: CircleId): Circle? = circles[circleId]
		override fun findAllByOwnerId(ownerId: MemberId): List<Circle> = circles.values.filter { it.ownerId == ownerId }
		override fun findAllByIds(circleIds: Collection<CircleId>): Map<CircleId, Circle> = circles.filter { it.key in circleIds }
	}

	private class FakeCircleMemberOutPort : CircleMemberOutPort {
		val members = mutableListOf<CircleMember>()
		override fun save(circleMember: CircleMember): CircleMember { members.removeIf { it.id == circleMember.id }; members.add(circleMember); return circleMember }
		override fun findByCircleAndMember(circleId: CircleId, memberId: MemberId): CircleMember? = members.firstOrNull { it.circleId == circleId && it.memberId == memberId }
		override fun findAllActiveByCircleId(circleId: CircleId): List<CircleMember> = members.filter { it.circleId == circleId && it.status == CircleMemberStatus.ACTIVE && !it.deleted }
		override fun findOldestActiveByCircleIdExcludingMemberId(circleId: CircleId, excludedMemberId: MemberId): CircleMember? =
			members
				.filter { it.circleId == circleId && it.status == CircleMemberStatus.ACTIVE && !it.deleted && it.memberId != excludedMemberId }
				.minWithOrNull(compareBy<CircleMember>({ it.joinedAt }, { it.memberId.toString() }))
		override fun findAllActiveByMemberId(memberId: MemberId): List<CircleMember> = members.filter { it.memberId == memberId && it.status == CircleMemberStatus.ACTIVE && !it.deleted }
		override fun existsByCircleAndMember(circleId: CircleId, memberId: MemberId): Boolean = members.any { it.circleId == circleId && it.memberId == memberId }
		override fun existsActiveByCircleAndMember(circleId: CircleId, memberId: MemberId): Boolean = members.any { it.circleId == circleId && it.memberId == memberId && it.status == com.unicorn.server.domain.circle.enums.CircleMemberStatus.ACTIVE && !it.deleted }
		override fun countActiveByCircleId(circleId: CircleId): Long = members.count { it.circleId == circleId && it.status == CircleMemberStatus.ACTIVE && !it.deleted }.toLong()
	}

	private class FakeMemberQueryInPort : GetMemberProfileInPort {
		private val members = linkedMapOf<MemberId, Member>()
		fun save(member: Member): Member { members[member.id] = member; return member }
		override fun getMemberProfile(memberId: String): MemberProfileDto? =
			members[MemberId.of(memberId)]?.let {
				MemberProfileDto(
					memberId = it.id.toString(),
					nickname = it.nickname,
					avatarColor = it.avatarColor,
					profileImageKey = it.profileImageKey,
					kakaoNickname = null,
					active = !it.isDeleted(),
				)
			}
	}

	private class RecordingEventPublisher : EventPublisher {
		override fun publish(event: com.unicorn.server.common.domain.Event) = Unit
	}
}
