package com.unicorn.server.domain.circle

import com.unicorn.server.TestIdFactory
import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.circle.enums.CircleMemberStatus
import com.unicorn.server.domain.circle.enums.CircleRole
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.domain.circle.exception.CircleSuccessorNotFoundException
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
import java.time.LocalDateTime

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

	@Test
	@DisplayName("일반 구성원이 써클을 탈퇴하면 멤버십이 soft delete 된다")
	fun leave_member_softDeletesMembership() {
		val owner = Member.create(Email("leave-owner@example.com"), "Owner", "leaveOwner", role = Role.MEMBER)
		val member = Member.create(Email("leaver@example.com"), "Member", "leaver", role = Role.MEMBER)
		memberQueryInPort.save(owner)
		memberQueryInPort.save(member)
		val circle = circleOutPort.save(Circle.create(TestIdFactory.circleId(), "탈퇴테스트", owner.id))
		circleMemberOutPort.save(CircleMember.createInitiator(TestIdFactory.circleMemberId(), circle.id, owner.id, owner.nickname))
		circleMemberOutPort.save(CircleMember.createMember(TestIdFactory.circleMemberId(), circle.id, member.id, member.nickname))

		circleService.leave(circle.id.toString(), member.id.toString())

		val membership = circleMemberOutPort.findByCircleAndMember(circle.id, member.id)
		assertThat(membership?.status).isEqualTo(CircleMemberStatus.LEFT)
		assertThat(membership?.deleted).isTrue()
		assertThat(membership?.leftAt).isNotNull()
		assertThat(circleService.isCircleMember(circle.id.toString(), member.id.toString())).isFalse()
	}

	@Test
	@DisplayName("방장이 탈퇴하면 가입일이 가장 오래된 활성 구성원에게 권한이 위임된다")
	fun leave_initiator_transfersRoleToOldestMember() {
		val owner = Member.create(Email("initiator-leave@example.com"), "Owner", "initiator", role = Role.MEMBER)
		val oldestMember = Member.create(Email("oldest@example.com"), "Oldest", "oldest", role = Role.MEMBER)
		val latestMember = Member.create(Email("latest@example.com"), "Latest", "latest", role = Role.MEMBER)
		memberQueryInPort.save(owner)
		memberQueryInPort.save(oldestMember)
		memberQueryInPort.save(latestMember)
		val circle = circleOutPort.save(Circle.create(TestIdFactory.circleId(), "방장탈퇴", owner.id))
		val baseTime = LocalDateTime.of(2026, 1, 1, 12, 0)
		circleMemberOutPort.save(circleMember(circle, owner, CircleRole.INITIATOR, baseTime))
		circleMemberOutPort.save(circleMember(circle, oldestMember, CircleRole.MEMBER, baseTime.plusMinutes(1)))
		circleMemberOutPort.save(circleMember(circle, latestMember, CircleRole.MEMBER, baseTime.plusMinutes(2)))

		circleService.leave(circle.id.toString(), owner.id.toString())

		assertThat(circleOutPort.findById(circle.id)?.ownerId).isEqualTo(oldestMember.id)
		assertThat(circleOutPort.findById(circle.id)?.deleted).isFalse()
		assertThat(circleMemberOutPort.findByCircleAndMember(circle.id, owner.id)?.status).isEqualTo(CircleMemberStatus.LEFT)
		assertThat(circleMemberOutPort.findByCircleAndMember(circle.id, owner.id)?.deleted).isTrue()
		assertThat(circleMemberOutPort.findByCircleAndMember(circle.id, oldestMember.id)?.role).isEqualTo(CircleRole.INITIATOR)
		assertThat(circleMemberOutPort.findByCircleAndMember(circle.id, latestMember.id)?.role).isEqualTo(CircleRole.MEMBER)
	}

	@Test
	@DisplayName("방장이 유일한 구성원인 상태에서 탈퇴하면 써클이 soft delete 된다")
	fun leave_singleInitiator_softDeletesCircle() {
		val owner = Member.create(Email("single-initiator@example.com"), "Owner", "single", role = Role.MEMBER)
		memberQueryInPort.save(owner)
		val circle = circleOutPort.save(Circle.create(TestIdFactory.circleId(), "단독방장탈퇴", owner.id))
		circleMemberOutPort.save(CircleMember.createInitiator(TestIdFactory.circleMemberId(), circle.id, owner.id, owner.nickname))

		circleService.leave(circle.id.toString(), owner.id.toString())

		assertThat(circleOutPort.findById(circle.id)?.deleted).isTrue()
		assertThat(circleMemberOutPort.findByCircleAndMember(circle.id, owner.id)?.status).isEqualTo(CircleMemberStatus.LEFT)
		assertThat(circleMemberOutPort.findByCircleAndMember(circle.id, owner.id)?.deleted).isTrue()
	}

	@Test
	@DisplayName("후임자 존재 확인 후 조회에 실패하면 써클을 soft delete 한다")
	fun leave_successorDisappearsDuringLookup_softDeletesCircle() {
		val owner = Member.create(Email("race-owner@example.com"), "Owner", "raceOwner", role = Role.MEMBER)
		val member = Member.create(Email("race-member@example.com"), "Member", "raceMember", role = Role.MEMBER)
		memberQueryInPort.save(owner)
		memberQueryInPort.save(member)
		val circle = circleOutPort.save(Circle.create(TestIdFactory.circleId(), "후임자조회경쟁", owner.id))
		circleMemberOutPort.save(CircleMember.createInitiator(TestIdFactory.circleMemberId(), circle.id, owner.id, owner.nickname))
		circleMemberOutPort.save(CircleMember.createMember(TestIdFactory.circleMemberId(), circle.id, member.id, member.nickname))
		circleMemberOutPort.successorDisappearsOnLookup = true

		circleService.leave(circle.id.toString(), owner.id.toString())

		assertThat(circleOutPort.findById(circle.id)?.deleted).isTrue()
		assertThat(circleMemberOutPort.findByCircleAndMember(circle.id, owner.id)?.status).isEqualTo(CircleMemberStatus.LEFT)
		assertThat(circleMemberOutPort.findByCircleAndMember(circle.id, owner.id)?.deleted).isTrue()
	}

	private fun circleMember(circle: Circle, member: Member, role: CircleRole, joinedAt: LocalDateTime): CircleMember =
		CircleMember(
			id = TestIdFactory.circleMemberId(),
			circleId = circle.id,
			memberId = member.id,
			nickname = member.nickname,
			role = role,
			status = CircleMemberStatus.ACTIVE,
			joinedAt = joinedAt,
			leftAt = null,
			deleted = false,
			createdAt = joinedAt,
			updatedAt = joinedAt,
		)

	private class FakeCircleOutPort : CircleOutPort {
		private val circles = linkedMapOf<CircleId, Circle>()
		override fun save(circle: Circle): Circle { circles[circle.id] = circle; return circle }
		override fun findById(circleId: CircleId): Circle? = circles[circleId]
		override fun findAllByOwnerId(ownerId: MemberId): List<Circle> = circles.values.filter { it.ownerId == ownerId }
		override fun findAllByIds(circleIds: Collection<CircleId>): Map<CircleId, Circle> = circles.filter { it.key in circleIds }
	}

	private class FakeCircleMemberOutPort : CircleMemberOutPort {
		val members = mutableListOf<CircleMember>()
		var successorDisappearsOnLookup = false
		override fun save(circleMember: CircleMember): CircleMember { members.removeIf { it.id == circleMember.id }; members.add(circleMember); return circleMember }
		override fun findByCircleAndMember(circleId: CircleId, memberId: MemberId): CircleMember? = members.firstOrNull { it.circleId == circleId && it.memberId == memberId }
		override fun findAllActiveByCircleId(circleId: CircleId): List<CircleMember> = members.filter { it.circleId == circleId && it.status == CircleMemberStatus.ACTIVE && !it.deleted }
		override fun existsActiveMemberByCircleIdExcludingMemberId(circleId: CircleId, excludedMemberId: MemberId): Boolean =
			members.any { it.circleId == circleId && it.status == CircleMemberStatus.ACTIVE && !it.deleted && it.role == CircleRole.MEMBER && it.memberId != excludedMemberId }
		override fun findOldestActiveByCircleIdExcludingMemberId(circleId: CircleId, excludedMemberId: MemberId): CircleMember {
			if (successorDisappearsOnLookup) {
				members.removeIf { it.circleId == circleId && it.role == CircleRole.MEMBER && it.memberId != excludedMemberId }
				throw CircleSuccessorNotFoundException(circleId.toString())
			}
			return members
				.filter { it.circleId == circleId && it.status == CircleMemberStatus.ACTIVE && !it.deleted && it.role == CircleRole.MEMBER && it.memberId != excludedMemberId }
				.minWithOrNull(compareBy<CircleMember>({ it.joinedAt }, { it.memberId.toString() }))
				?: throw CircleSuccessorNotFoundException(circleId.toString())
		}
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
