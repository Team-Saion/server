package com.unicorn.server.infrastructure.adapter.out.persistence.circle

import com.unicorn.server.domain.circle.Circle
import com.unicorn.server.domain.circle.CircleMember
import com.unicorn.server.domain.circle.enums.CircleRole
import com.unicorn.server.domain.circle.exception.CircleSuccessorNotFoundException
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.circle.vo.CircleMemberId
import com.unicorn.server.domain.member.vo.MemberId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("CircleAccessPersistenceAdapter 통합 테스트")
class CircleAccessPersistenceAdapterTest(
	@param:Autowired private val circleAccessPersistenceAdapter: CircleAccessPersistenceAdapter,
	@param:Autowired private val circlePersistenceAdapter: CirclePersistenceAdapter,
	@param:Autowired private val circleMemberPersistenceAdapter: CircleMemberPersistenceAdapter,
) {

	@Test
	@DisplayName("활성 써클이면 existsById가 true를 반환한다")
	fun existsById_withActiveCircle_returnsTrue() {
		val circleId = seedCircle("CC000000000000000301")

		val result = circleAccessPersistenceAdapter.existsById(circleId)

		assertThat(result).isTrue()
	}

	@Test
	@DisplayName("삭제된 써클이면 existsById가 false를 반환한다")
	fun existsById_withDeletedCircle_returnsFalse() {
		val circle = Circle.create(CircleId.of("CC000000000000000302"), "삭제된써클", MemberId.generate())
		circle.delete()
		circlePersistenceAdapter.save(circle)

		val result = circleAccessPersistenceAdapter.existsById("CC000000000000000302")

		assertThat(result).isFalse()
	}

	@Test
	@DisplayName("존재하지 않는 써클이면 existsById가 false를 반환한다")
	fun existsById_withUnknownCircle_returnsFalse() {
		val result = circleAccessPersistenceAdapter.existsById("CC000000000000000303")

		assertThat(result).isFalse()
	}

	@Test
	@DisplayName("활성 멤버면 isMember가 true를 반환한다")
	fun isMember_withActiveMember_returnsTrue() {
		val circleId = seedCircle("CC000000000000000304")
		val memberId = seedMember(circleId, "CM000000000000000304")

		val result = circleAccessPersistenceAdapter.isMember(circleId, memberId)

		assertThat(result).isTrue()
	}

	@Test
	@DisplayName("탈퇴한 멤버면 isMember가 false를 반환한다")
	fun isMember_withLeftMember_returnsFalse() {
		val circleId = seedCircle("CC000000000000000305")
		val member = CircleMember.createMember(
			id = CircleMemberId.of("CM000000000000000305"),
			circleId = CircleId.of(circleId),
			memberId = MemberId.generate(),
			nickname = "탈퇴멤버",
		)
		member.leave()
		circleMemberPersistenceAdapter.save(member)

		val result = circleAccessPersistenceAdapter.isMember(circleId, member.memberId.toString())

		assertThat(result).isFalse()
	}

	@Test
	@DisplayName("써클에 속하지 않은 멤버면 isMember가 false를 반환한다")
	fun isMember_withNonMember_returnsFalse() {
		val circleId = seedCircle("CC000000000000000306")

		val result = circleAccessPersistenceAdapter.isMember(circleId, MemberId.generate().toString())

		assertThat(result).isFalse()
	}

	@Test
	@DisplayName("모임장이면 isInitiator가 true를 반환한다")
	fun isInitiator_withInitiator_returnsTrue() {
		val circleId = seedCircle("CC000000000000000307")
		val initiator = CircleMember.createInitiator(
			id = CircleMemberId.of("CM000000000000000307"),
			circleId = CircleId.of(circleId),
			memberId = MemberId.generate(),
			nickname = "모임장",
		)
		circleMemberPersistenceAdapter.save(initiator)

		val result = circleAccessPersistenceAdapter.isInitiator(circleId, initiator.memberId.toString())

		assertThat(result).isTrue()
	}

	@Test
	@DisplayName("일반 멤버면 isInitiator가 false를 반환한다")
	fun isInitiator_withRegularMember_returnsFalse() {
		val circleId = seedCircle("CC000000000000000308")
		val memberId = seedMember(circleId, "CM000000000000000308")

		val result = circleAccessPersistenceAdapter.isInitiator(circleId, memberId)

		assertThat(result).isFalse()
	}

	@Test
	@DisplayName("후임자 조회는 기존 방장을 제외하고 MEMBER 역할의 구성원을 반환한다")
	fun findOldestActiveByCircleIdExcludingMemberId_excludesInitiator() {
		val circleId = seedCircle("CC000000000000000309")
		val initiator = CircleMember.createInitiator(
			id = CircleMemberId.of("CM000000000000000309"),
			circleId = CircleId.of(circleId),
			memberId = MemberId.generate(),
			nickname = "기존방장",
		)
		val member = CircleMember.createMember(
			id = CircleMemberId.of("CM000000000000000310"),
			circleId = CircleId.of(circleId),
			memberId = MemberId.generate(),
			nickname = "후임자",
		)
		circleMemberPersistenceAdapter.save(initiator)
		circleMemberPersistenceAdapter.save(member)

		val exists = circleMemberPersistenceAdapter.existsActiveMemberByCircleIdExcludingMemberId(
			CircleId.of(circleId),
			initiator.memberId,
		)
		val successor = circleMemberPersistenceAdapter.findOldestActiveByCircleIdExcludingMemberId(
			CircleId.of(circleId),
			initiator.memberId,
		)

		assertThat(exists).isTrue()
		assertThat(successor.memberId).isEqualTo(member.memberId)
		assertThat(successor.role).isEqualTo(CircleRole.MEMBER)
	}

	@Test
	@DisplayName("써클에 기존 방장만 있으면 후임자가 존재하지 않는다")
	fun existsActiveMemberByCircleIdExcludingMemberId_withOnlyInitiator_returnsFalse() {
		val circleId = seedCircle("CC000000000000000310")
		val initiator = CircleMember.createInitiator(
			id = CircleMemberId.of("CM000000000000000311"),
			circleId = CircleId.of(circleId),
			memberId = MemberId.generate(),
			nickname = "단독방장",
		)
		circleMemberPersistenceAdapter.save(initiator)

		val result = circleMemberPersistenceAdapter.existsActiveMemberByCircleIdExcludingMemberId(
			CircleId.of(circleId),
			initiator.memberId,
		)

		assertThat(result).isFalse()
		assertThatThrownBy {
			circleMemberPersistenceAdapter.findOldestActiveByCircleIdExcludingMemberId(
				CircleId.of(circleId),
				initiator.memberId,
			)
		}.isInstanceOf(CircleSuccessorNotFoundException::class.java)
	}

	private fun seedCircle(circleId: String): String {
		circlePersistenceAdapter.save(
			Circle.create(CircleId.of(circleId), "테스트써클", MemberId.generate()),
		)
		return circleId
	}

	private fun seedMember(circleId: String, circleMemberId: String): String {
		val member = CircleMember.createMember(
			id = CircleMemberId.of(circleMemberId),
			circleId = CircleId.of(circleId),
			memberId = MemberId.generate(),
			nickname = "일반멤버",
		)
		circleMemberPersistenceAdapter.save(member)
		return member.memberId.toString()
	}
}
