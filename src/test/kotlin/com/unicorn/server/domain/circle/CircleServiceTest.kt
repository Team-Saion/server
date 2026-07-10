package com.unicorn.server.domain.circle

import com.unicorn.server.TestIdFactory
import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.domain.Event
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.circle.enums.CircleRole
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.domain.circle.event.CircleInitiatorTransferredEvent
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
import org.assertj.core.api.Assertions.assertThatThrownBy
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
		val owner = Member.create(Email("owner@example.com"), "Owner", "비니", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		memberQueryInPort.save(owner)

		val result = circleService.create(owner.id.toString(), CreateCircleCommand("비니네"))

		assertThat(result.name).isEqualTo("비니네")
		assertThat(circleMemberOutPort.members).hasSize(1)
		assertThat(circleMemberOutPort.members.first().memberId).isEqualTo(owner.id)
	}

	@Test
	@DisplayName("현재 활성 써클이 있으면 새 써클을 생성할 수 없다")
	fun create_withActiveCircle_throwsException() {
		val owner = Member.create(Email("owner2@example.com"), "Owner2", "비니", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		memberQueryInPort.save(owner)
		val existingCircle = circleOutPort.save(Circle.create(TestIdFactory.circleId(), "기존써클", owner.id))
		circleMemberOutPort.save(com.unicorn.server.domain.circle.CircleMember.createInitiator(TestIdFactory.circleMemberId(), existingCircle.id, owner.id, owner.nickname))

		assertThatThrownBy { circleService.create(owner.id.toString(), CreateCircleCommand("새써클")) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.ALREADY_HAS_ACTIVE_CIRCLE)
	}

	@Test
	@DisplayName("내가 속한 모든 활성 써클 목록을 조회한다")
	fun listCircles_returnsAllActiveMemberCircles() {
		val member = Member.create(Email("member@example.com"), "Member", "멤버", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		val otherMember = Member.create(Email("other@example.com"), "Other", "다른멤버", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		memberQueryInPort.save(member)
		memberQueryInPort.save(otherMember)

		val myCircle = circleOutPort.save(Circle.create(TestIdFactory.circleId(), "내써클", otherMember.id))
		val anotherCircle = circleOutPort.save(Circle.create(TestIdFactory.circleId(), "또다른써클", member.id))
		circleMemberOutPort.save(com.unicorn.server.domain.circle.CircleMember.createMember(TestIdFactory.circleMemberId(), myCircle.id, member.id, member.nickname))
		circleMemberOutPort.save(com.unicorn.server.domain.circle.CircleMember.createInitiator(TestIdFactory.circleMemberId(), anotherCircle.id, member.id, member.nickname))

		val result = circleService.listCircles(member.id.toString())

		assertThat(result.map { it.id }).containsExactly(anotherCircle.id.toString(), myCircle.id.toString())
	}

	@Test
	@DisplayName("현재 방장이 같은 써클의 다른 활성 구성원에게 권한을 위임할 수 있다")
	fun transferInitiator_success() {
		val owner = Member.create(Email("owner3@example.com"), "Owner3", "오너삼", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		val target = Member.create(Email("target@example.com"), "Target", "타겟", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		memberQueryInPort.save(owner)
		memberQueryInPort.save(target)

		val circle = circleOutPort.save(Circle.create(TestIdFactory.circleId(), "위임써클", owner.id))
		val initiator = circleMemberOutPort.save(com.unicorn.server.domain.circle.CircleMember.createInitiator(TestIdFactory.circleMemberId(), circle.id, owner.id, owner.nickname))
		val member = circleMemberOutPort.save(com.unicorn.server.domain.circle.CircleMember.createMember(TestIdFactory.circleMemberId(), circle.id, target.id, target.nickname))

		val result = circleService.transferInitiator(circle.id.toString(), owner.id.toString(), target.id.toString())

		assertThat(result.ownerId).isEqualTo(target.id.toString())
		assertThat(circleOutPort.findById(circle.id)?.ownerId).isEqualTo(target.id)
		assertThat(circleMemberOutPort.findByCircleAndMember(circle.id, owner.id)?.role).isEqualTo(CircleRole.MEMBER)
		assertThat(circleMemberOutPort.findByCircleAndMember(circle.id, target.id)?.role).isEqualTo(CircleRole.INITIATOR)
		assertThat(eventPublisher.events.filterIsInstance<CircleInitiatorTransferredEvent>()).hasSize(1)
		assertThat(initiator.id).isNotNull()
		assertThat(member.id).isNotNull()
	}

	@Test
	@DisplayName("방장이 아닌 사용자는 권한을 위임할 수 없다")
	fun transferInitiator_nonInitiator_throwsException() {
		val owner = Member.create(Email("owner4@example.com"), "Owner4", "오너사", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		val requester = Member.create(Email("requester@example.com"), "Requester", "요청자", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		val target = Member.create(Email("target2@example.com"), "Target2", "타겟이", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		memberQueryInPort.save(owner)
		memberQueryInPort.save(requester)
		memberQueryInPort.save(target)

		val circle = circleOutPort.save(Circle.create(TestIdFactory.circleId(), "권한없음써클", owner.id))
		circleMemberOutPort.save(com.unicorn.server.domain.circle.CircleMember.createInitiator(TestIdFactory.circleMemberId(), circle.id, owner.id, owner.nickname))
		circleMemberOutPort.save(com.unicorn.server.domain.circle.CircleMember.createMember(TestIdFactory.circleMemberId(), circle.id, requester.id, requester.nickname))
		circleMemberOutPort.save(com.unicorn.server.domain.circle.CircleMember.createMember(TestIdFactory.circleMemberId(), circle.id, target.id, target.nickname))

		assertThatThrownBy { circleService.transferInitiator(circle.id.toString(), requester.id.toString(), target.id.toString()) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.INITIATOR_DELEGATION_FORBIDDEN)
	}

	@Test
	@DisplayName("자기 자신에게는 권한을 위임할 수 없다")
	fun transferInitiator_self_throwsException() {
		val owner = Member.create(Email("owner5@example.com"), "Owner5", "오너오", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		memberQueryInPort.save(owner)

		val circle = circleOutPort.save(Circle.create(TestIdFactory.circleId(), "셀프위임써클", owner.id))
		circleMemberOutPort.save(com.unicorn.server.domain.circle.CircleMember.createInitiator(TestIdFactory.circleMemberId(), circle.id, owner.id, owner.nickname))

		assertThatThrownBy { circleService.transferInitiator(circle.id.toString(), owner.id.toString(), owner.id.toString()) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.INITIATOR_DELEGATION_SELF_FORBIDDEN)
	}

	@Test
	@DisplayName("탈퇴한 구성원에게는 권한을 위임할 수 없다")
	fun transferInitiator_leftMember_throwsException() {
		val owner = Member.create(Email("owner6@example.com"), "Owner6", "오너육", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		val target = Member.create(Email("target3@example.com"), "Target3", "타겟삼", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		memberQueryInPort.save(owner)
		memberQueryInPort.save(target)

		val circle = circleOutPort.save(Circle.create(TestIdFactory.circleId(), "탈퇴대상써클", owner.id))
		circleMemberOutPort.save(com.unicorn.server.domain.circle.CircleMember.createInitiator(TestIdFactory.circleMemberId(), circle.id, owner.id, owner.nickname))
		val leftMember = com.unicorn.server.domain.circle.CircleMember.createMember(TestIdFactory.circleMemberId(), circle.id, target.id, target.nickname)
		leftMember.leave()
		circleMemberOutPort.save(leftMember)

		assertThatThrownBy { circleService.transferInitiator(circle.id.toString(), owner.id.toString(), target.id.toString()) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.INITIATOR_DELEGATION_TARGET_INVALID)
	}

	@Test
	@DisplayName("방장이 탈퇴하면 가장 먼저 참여한 활성 멤버에게 권한이 자동 위임된다")
	fun handleMemberWithdrawal_initiatorTransfersToOldestMember() {
		val owner = Member.create(Email("owner7@example.com"), "Owner7", "오너칠", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		val firstJoiner = Member.create(Email("first@example.com"), "First", "첫째", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		val secondJoiner = Member.create(Email("second@example.com"), "Second", "둘째", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		memberQueryInPort.save(owner)
		memberQueryInPort.save(firstJoiner)
		memberQueryInPort.save(secondJoiner)

		val circle = circleOutPort.save(Circle.create(TestIdFactory.circleId(), "자동위임써클", owner.id))
		val baseTime = LocalDateTime.of(2026, 1, 1, 12, 0)
		val initiator = circleMemberOutPort.save(
			CircleMember(
				id = TestIdFactory.circleMemberId(),
				circleId = circle.id,
				memberId = owner.id,
				nickname = owner.nickname,
				role = CircleRole.INITIATOR,
				status = com.unicorn.server.domain.circle.enums.CircleMemberStatus.ACTIVE,
				joinedAt = baseTime,
				leftAt = null,
				deleted = false,
				createdAt = baseTime,
				updatedAt = baseTime,
			),
		)
		val oldestMember = circleMemberOutPort.save(
			CircleMember(
				id = TestIdFactory.circleMemberId(),
				circleId = circle.id,
				memberId = firstJoiner.id,
				nickname = firstJoiner.nickname,
				role = CircleRole.MEMBER,
				status = com.unicorn.server.domain.circle.enums.CircleMemberStatus.ACTIVE,
				joinedAt = baseTime.plusMinutes(1),
				leftAt = null,
				deleted = false,
				createdAt = baseTime.plusMinutes(1),
				updatedAt = baseTime.plusMinutes(1),
			),
		)
		val latestMember = circleMemberOutPort.save(
			CircleMember(
				id = TestIdFactory.circleMemberId(),
				circleId = circle.id,
				memberId = secondJoiner.id,
				nickname = secondJoiner.nickname,
				role = CircleRole.MEMBER,
				status = com.unicorn.server.domain.circle.enums.CircleMemberStatus.ACTIVE,
				joinedAt = baseTime.plusMinutes(2),
				leftAt = null,
				deleted = false,
				createdAt = baseTime.plusMinutes(2),
				updatedAt = baseTime.plusMinutes(2),
			),
		)

		circleService.handleMemberWithdrawal(owner.id.toString())

		assertThat(circleOutPort.findById(circle.id)?.deleted).isFalse()
		assertThat(circleOutPort.findById(circle.id)?.ownerId).isEqualTo(firstJoiner.id)
		assertThat(circleMemberOutPort.findByCircleAndMember(circle.id, owner.id)?.status).isEqualTo(com.unicorn.server.domain.circle.enums.CircleMemberStatus.LEFT)
		assertThat(circleMemberOutPort.findByCircleAndMember(circle.id, owner.id)?.role).isEqualTo(CircleRole.MEMBER)
		assertThat(circleMemberOutPort.findByCircleAndMember(circle.id, firstJoiner.id)?.role).isEqualTo(CircleRole.INITIATOR)
		assertThat(circleMemberOutPort.findByCircleAndMember(circle.id, secondJoiner.id)?.role).isEqualTo(CircleRole.MEMBER)
		assertThat(eventPublisher.events.filterIsInstance<CircleInitiatorTransferredEvent>()).hasSize(1)
		assertThat(initiator.id).isNotNull()
		assertThat(oldestMember.id).isNotNull()
		assertThat(latestMember.id).isNotNull()
	}

	@Test
	@DisplayName("방장이 유일한 참여자인 상태에서 탈퇴하면 써클이 삭제된다")
	fun handleMemberWithdrawal_singleInitiatorSoftDeletesCircle() {
		val owner = Member.create(Email("owner8@example.com"), "Owner8", "오너팔", role = com.unicorn.server.domain.member.enums.Role.MEMBER)
		memberQueryInPort.save(owner)

		val circle = circleOutPort.save(Circle.create(TestIdFactory.circleId(), "삭제써클", owner.id))
		circleMemberOutPort.save(com.unicorn.server.domain.circle.CircleMember.createInitiator(TestIdFactory.circleMemberId(), circle.id, owner.id, owner.nickname))

		circleService.handleMemberWithdrawal(owner.id.toString())

		assertThat(circleOutPort.findById(circle.id)?.deleted).isTrue()
		assertThat(circleMemberOutPort.findByCircleAndMember(circle.id, owner.id)?.status).isEqualTo(com.unicorn.server.domain.circle.enums.CircleMemberStatus.LEFT)
		assertThat(eventPublisher.events.filterIsInstance<CircleInitiatorTransferredEvent>()).isEmpty()
	}

	private class FakeCircleOutPort : CircleOutPort {
		private val circles = linkedMapOf<CircleId, Circle>()
		override fun save(circle: Circle): Circle {
			circles[circle.id] = circle
			return circle
		}
		override fun findById(circleId: CircleId): Circle? = circles[circleId]
		override fun findAllByOwnerId(ownerId: MemberId): List<Circle> = circles.values.filter { it.ownerId == ownerId }
		override fun findAllByIds(circleIds: Collection<CircleId>): Map<CircleId, Circle> = circles.filter { it.key in circleIds }
	}

	private class FakeCircleMemberOutPort : CircleMemberOutPort {
		val members = mutableListOf<com.unicorn.server.domain.circle.CircleMember>()
		override fun save(circleMember: com.unicorn.server.domain.circle.CircleMember): com.unicorn.server.domain.circle.CircleMember {
			members.removeIf { it.id == circleMember.id }
			members.add(circleMember)
			return circleMember
		}
		override fun findByCircleAndMember(circleId: CircleId, memberId: MemberId) = members.firstOrNull { it.circleId == circleId && it.memberId == memberId }
		override fun findAllActiveByCircleId(circleId: CircleId) = members.filter { it.circleId == circleId && it.status == com.unicorn.server.domain.circle.enums.CircleMemberStatus.ACTIVE && !it.deleted }
		override fun findAllActiveByMemberId(memberId: MemberId) = members.filter { it.memberId == memberId && it.status == com.unicorn.server.domain.circle.enums.CircleMemberStatus.ACTIVE && !it.deleted }
		override fun existsByCircleAndMember(circleId: CircleId, memberId: MemberId) = members.any { it.circleId == circleId && it.memberId == memberId }
		override fun existsActiveByCircleAndMember(circleId: CircleId, memberId: MemberId) = members.any { it.circleId == circleId && it.memberId == memberId && it.status == com.unicorn.server.domain.circle.enums.CircleMemberStatus.ACTIVE && !it.deleted }
		override fun countActiveByCircleId(circleId: CircleId) = members.count { it.circleId == circleId && it.status == com.unicorn.server.domain.circle.enums.CircleMemberStatus.ACTIVE && !it.deleted }.toLong()
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
		val events = mutableListOf<Event>()

		override fun publish(event: Event) {
			events += event
		}
	}
}
