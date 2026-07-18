package com.unicorn.server.domain.circle

import com.unicorn.server.TestIdFactory
import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.circle.enums.CircleMemberStatus
import com.unicorn.server.domain.circle.enums.CircleRole
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.domain.member.vo.MemberId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Circle/CircleMember 정책 단위 테스트")
class CirclePolicyTest {
	@Test
	@DisplayName("유효한 써클 이름으로 생성 시 trim된 이름이 저장된다")
	fun create_withValidName_success() {
		val circle = Circle.create(TestIdFactory.circleId(), " 비니abc123 ", TestIdFactory.memberId())

		assertThat(circle.name).isEqualTo("비니abc123")
	}

	@Test
	@DisplayName("이모지와 특수문자가 포함된 써클 이름으로 생성할 수 있다")
	fun create_withSpecialCharactersAndEmoji_success() {
		val circle = Circle.create(TestIdFactory.circleId(), "비니네! 🎉 #1", TestIdFactory.memberId())

		assertThat(circle.name).isEqualTo("비니네! 🎉 #1")
	}

	@Test
	@DisplayName("보안상 허용되지 않는 문자가 포함된 써클 이름으로 생성 시 예외가 발생한다")
	fun create_withForbiddenCharacters_throwsException() {
		assertThatThrownBy { Circle.create(TestIdFactory.circleId(), "비니네<script>", TestIdFactory.memberId()) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.CIRCLE_NAME_INVALID_CHARSET)
	}

	@Test
	@DisplayName("공백만 있는 써클 이름으로 생성 시 예외가 발생한다")
	fun create_withBlankName_throwsException() {
		assertThatThrownBy { Circle.create(TestIdFactory.circleId(), "   ", TestIdFactory.memberId()) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.CIRCLE_NAME_BLANK)
	}

	@Test
	@DisplayName("20자를 초과하는 써클 이름으로 생성 시 예외가 발생한다")
	fun create_withTooLongName_throwsException() {
		assertThatThrownBy { Circle.create(TestIdFactory.circleId(), "😀".repeat(21), TestIdFactory.memberId()) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.CIRCLE_NAME_TOO_LONG)
	}

	@Test
	@DisplayName("30자를 초과하는 써클 멤버 닉네임으로 생성 시 예외가 발생한다")
	fun createMember_withTooLongNickname_throwsException() {
		assertThatThrownBy {
			CircleMember.createMember(
				id = TestIdFactory.circleMemberId(),
				circleId = TestIdFactory.circleId(),
				memberId = TestIdFactory.memberId(),
				nickname = "a".repeat(31),
			)
		}
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.CIRCLE_NICKNAME_INVALID)
	}

	@Test
	@DisplayName("새로운 방장에게 권한을 위임하면 써클 소유자와 구성원 역할이 함께 변경된다")
	fun transferInitiator_changesOwnerAndMemberRoles() {
		val ownerId = TestIdFactory.memberId()
		val newInitiatorId = TestIdFactory.memberId()
		val circle = Circle.create(TestIdFactory.circleId(), "권한위임", ownerId)
		val currentInitiator = CircleMember.createInitiator(
			TestIdFactory.circleMemberId(),
			circle.id,
			ownerId,
			"기존방장",
		)
		val newInitiator = CircleMember.createMember(
			TestIdFactory.circleMemberId(),
			circle.id,
			newInitiatorId,
			"새방장",
		)

		circle.transferInitiator(currentInitiator, newInitiator)

		assertThat(circle.ownerId).isEqualTo(newInitiatorId)
		assertThat(currentInitiator.role).isEqualTo(CircleRole.MEMBER)
		assertThat(newInitiator.role).isEqualTo(CircleRole.INITIATOR)
	}

	@Test
	@DisplayName("방장이 탈퇴하면 후임자 승격과 탈퇴 상태 변경을 함께 처리한다")
	fun leaveMember_initiator_transfersRoleAndLeaves() {
		val ownerId = TestIdFactory.memberId()
		val successorId = TestIdFactory.memberId()
		val circle = Circle.create(TestIdFactory.circleId(), "방장탈퇴", ownerId)
		val leavingMember = CircleMember.createInitiator(
			TestIdFactory.circleMemberId(),
			circle.id,
			ownerId,
			"기존방장",
		)
		val successor = CircleMember.createMember(
			TestIdFactory.circleMemberId(),
			circle.id,
			successorId,
			"후임방장",
		)

		val result = circle.leaveMember(leavingMember, successor)

		assertThat(result).isSameAs(successor)
		assertThat(circle.ownerId).isEqualTo(successorId)
		assertThat(circle.deleted).isFalse()
		assertThat(leavingMember.role).isEqualTo(CircleRole.MEMBER)
		assertThat(leavingMember.status).isEqualTo(CircleMemberStatus.LEFT)
		assertThat(leavingMember.deleted).isTrue()
		assertThat(successor.role).isEqualTo(CircleRole.INITIATOR)
	}

	@Test
	@DisplayName("후임자가 없는 마지막 방장이 탈퇴하면 써클과 멤버십을 함께 soft delete 한다")
	fun deleteWithLastMember_deletesCircleAndMembership() {
		val ownerId = TestIdFactory.memberId()
		val circle = Circle.create(TestIdFactory.circleId(), "단독방장", ownerId)
		val leavingMember = CircleMember.createInitiator(
			TestIdFactory.circleMemberId(),
			circle.id,
			ownerId,
			"단독방장",
		)

		circle.deleteWithLastMember(leavingMember)

		assertThat(circle.deleted).isTrue()
		assertThat(leavingMember.role).isEqualTo(CircleRole.MEMBER)
		assertThat(leavingMember.status).isEqualTo(CircleMemberStatus.LEFT)
		assertThat(leavingMember.deleted).isTrue()
	}
}
