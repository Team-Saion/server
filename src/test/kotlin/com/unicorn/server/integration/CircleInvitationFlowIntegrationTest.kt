package com.unicorn.server.integration

import com.unicorn.server.BaseTest
import com.unicorn.server.TestIdFactory
import com.unicorn.server.domain.circle.port.dto.CreateCircleCommand
import com.unicorn.server.domain.circle.port.`in`.CircleInPort
import com.unicorn.server.domain.home.port.`in`.HomeQueryInPort
import com.unicorn.server.domain.invitation.enums.InvitationChannel
import com.unicorn.server.domain.invitation.enums.InvitationType
import com.unicorn.server.domain.invitation.exception.InvitationSelfApprovalForbiddenException
import com.unicorn.server.domain.invitation.port.dto.DispatchInvitationCommand
import com.unicorn.server.domain.invitation.port.dto.IssueInvitationCommand
import com.unicorn.server.domain.invitation.port.`in`.AcceptCircleInvitationInPort
import com.unicorn.server.domain.invitation.port.`in`.DispatchInvitationInPort
import com.unicorn.server.domain.invitation.port.`in`.GetInvitationByTokenInPort
import com.unicorn.server.domain.invitation.port.`in`.IssueInvitationInPort
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.infrastructure.adapter.out.persistence.invitation.InvitationClickLogJpaRepository
import com.unicorn.server.infrastructure.adapter.out.persistence.invitation.InvitationDispatchLogJpaRepository
import com.unicorn.server.infrastructure.adapter.out.persistence.invitation.InvitationRedemptionLogJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Circle/Invitation 통합 테스트")
class CircleInvitationFlowIntegrationTest : BaseTest() {
	@Autowired private lateinit var memberOutPort: MemberOutPort
	@Autowired private lateinit var circleInPort: CircleInPort
	@Autowired private lateinit var homeQueryInPort: HomeQueryInPort
	@Autowired private lateinit var issueInvitationInPort: IssueInvitationInPort
	@Autowired private lateinit var dispatchInvitationInPort: DispatchInvitationInPort
	@Autowired private lateinit var getInvitationByTokenInPort: GetInvitationByTokenInPort
	@Autowired private lateinit var acceptCircleInvitationInPort: AcceptCircleInvitationInPort
	@Autowired private lateinit var invitationDispatchLogJpaRepository: InvitationDispatchLogJpaRepository
	@Autowired private lateinit var invitationClickLogJpaRepository: InvitationClickLogJpaRepository
	@Autowired private lateinit var invitationRedemptionLogJpaRepository: InvitationRedemptionLogJpaRepository

	@Test
	@DisplayName("초대 발급 후 두 명이 같은 링크로 순차 수락할 수 있고 로그가 모두 저장된다")
	fun multiUseInvitation_flow_success() {
		val initialDispatchCount = invitationDispatchLogJpaRepository.count()
		val initialClickCount = invitationClickLogJpaRepository.count()
		val initialRedemptionCount = invitationRedemptionLogJpaRepository.count()
		val owner = memberOutPort.save(member("Owner", "ownerA"))
		val invitee1 = memberOutPort.save(member("Invitee1", "inviteeA"))
		val invitee2 = memberOutPort.save(member("Invitee2", "inviteeB"))

		val circle = circleInPort.create(owner.id.toString(), CreateCircleCommand("멀티초대1"))
		val issued = issueInvitationInPort.issue(
			owner.id.toString(),
			IssueInvitationCommand(InvitationType.CIRCLE, circle.id, null, null),
		)

		dispatchInvitationInPort.dispatch(DispatchInvitationCommand(issued.invitationId, InvitationChannel.KAKAO))
		getInvitationByTokenInPort.getByToken(issued.token)
		acceptCircleInvitationInPort.accept(issued.token, invitee1.id.toString())
		acceptCircleInvitationInPort.accept(issued.token, invitee2.id.toString())

		val home = homeQueryInPort.getHome(circle.id, owner.id.toString())

		assertThat(home.members).hasSize(3)
		assertThat(invitationDispatchLogJpaRepository.count() - initialDispatchCount).isEqualTo(1)
		assertThat(invitationClickLogJpaRepository.count() - initialClickCount).isEqualTo(1)
		assertThat(invitationRedemptionLogJpaRepository.count() - initialRedemptionCount).isEqualTo(2)
	}

	@Test
	@DisplayName("같은 사용자가 같은 링크를 다시 수락하면 예외를 던지고 수락 로그가 늘지 않는다")
	fun reaccept_sameMember_throwsException() {
		val initialRedemptionCount = invitationRedemptionLogJpaRepository.count()
		val owner = memberOutPort.save(member("Owner4", "ownerD"))
		val invitee = memberOutPort.save(member("Invitee4", "inviteeD"))
		val circle = circleInPort.create(owner.id.toString(), CreateCircleCommand("재수락테스트4"))
		val issued = issueInvitationInPort.issue(
			owner.id.toString(),
			IssueInvitationCommand(InvitationType.CIRCLE, circle.id, null, null),
		)

		acceptCircleInvitationInPort.accept(issued.token, invitee.id.toString())
		org.junit.jupiter.api.assertThrows<com.unicorn.server.common.exception.BusinessException> {
			acceptCircleInvitationInPort.accept(issued.token, invitee.id.toString())
		}

		assertThat(invitationRedemptionLogJpaRepository.count() - initialRedemptionCount).isEqualTo(1)
	}

	@Test
	@DisplayName("초대장 조회와 발송은 각각 클릭 로그와 발송 로그를 남긴다")
	fun dispatchAndLookup_logsPersisted() {
		val initialDispatchCount = invitationDispatchLogJpaRepository.count()
		val initialClickCount = invitationClickLogJpaRepository.count()
		val owner = memberOutPort.save(member("Owner5", "ownerE"))
		val circle = circleInPort.create(owner.id.toString(), CreateCircleCommand("로그테스트5"))
		val issued = issueInvitationInPort.issue(
			owner.id.toString(),
			IssueInvitationCommand(InvitationType.CIRCLE, circle.id, "엄마", "같이해요"),
		)

		dispatchInvitationInPort.dispatch(DispatchInvitationCommand(issued.invitationId, InvitationChannel.MESSAGE))
		val detail = getInvitationByTokenInPort.getByToken(issued.token)

		assertThat(detail.circleName).isEqualTo("로그테스트5")
		assertThat(detail.inviterNickname).isEqualTo(owner.nickname)
		assertThat(invitationDispatchLogJpaRepository.count() - initialDispatchCount).isEqualTo(1)
		assertThat(invitationClickLogJpaRepository.count() - initialClickCount).isEqualTo(1)
	}

	@Test
	@DisplayName("자기 자신이 발급한 초대장을 자신이 수락하면 예외가 발생한다")
	fun selfInviteAccept_throwsException() {
		val owner = memberOutPort.save(member("Owner2", "ownerB"))
		val circle = circleInPort.create(owner.id.toString(), CreateCircleCommand("자기초대2"))
		val issued = issueInvitationInPort.issue(
			owner.id.toString(),
			IssueInvitationCommand(InvitationType.CIRCLE, circle.id, null, null),
		)

		assertThatThrownBy { acceptCircleInvitationInPort.accept(issued.token, owner.id.toString()) }
			.isInstanceOf(InvitationSelfApprovalForbiddenException::class.java)
	}

	@Test
	@DisplayName("탈퇴한 멤버는 써클 홈 구성원 목록에서 제외된다")
	fun withdrawnMember_hiddenFromHome() {
		val owner = memberOutPort.save(member("Owner3", "ownerC"))
		val invitee = memberOutPort.save(member("Invitee3", "inviteeC"))
		val circle = circleInPort.create(owner.id.toString(), CreateCircleCommand("탈퇴필터3"))
		val issued = issueInvitationInPort.issue(
			owner.id.toString(),
			IssueInvitationCommand(InvitationType.CIRCLE, circle.id, null, null),
		)
		acceptCircleInvitationInPort.accept(issued.token, invitee.id.toString())

		invitee.withdraw()
		memberOutPort.save(invitee)

		val home = homeQueryInPort.getHome(circle.id, owner.id.toString())

		assertThat(home.members).hasSize(1)
		assertThat(home.members.single().memberId).isEqualTo(owner.id.toString())
	}

	private fun member(name: String, nickname: String): Member =
		Member.create(TestIdFactory.memberId(), null, name, nickname, role = Role.MEMBER)
}
