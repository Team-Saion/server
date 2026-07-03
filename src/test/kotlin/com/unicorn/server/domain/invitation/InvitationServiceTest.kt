package com.unicorn.server.domain.invitation

import com.unicorn.server.TestIdFactory
import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.circle.port.dto.CircleMemberDto
import com.unicorn.server.domain.circle.port.dto.CircleSummary
import com.unicorn.server.domain.circle.port.dto.CreateCircleCommand
import com.unicorn.server.domain.circle.port.`in`.CircleInPort
import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.invitation.enums.InvitationChannel
import com.unicorn.server.domain.invitation.enums.InvitationType
import com.unicorn.server.domain.invitation.exception.InvitationErrorCode
import com.unicorn.server.domain.invitation.exception.InvitationExpiredException
import com.unicorn.server.domain.invitation.port.dto.DispatchInvitationCommand
import com.unicorn.server.domain.invitation.port.dto.IssueInvitationCommand
import com.unicorn.server.domain.invitation.port.out.InvitationIdGenerator
import com.unicorn.server.domain.invitation.port.out.InvitationOutPort
import com.unicorn.server.domain.invitation.port.out.InvitationTokenGenerator
import com.unicorn.server.domain.invitation.service.InvitationService
import com.unicorn.server.domain.invitation.vo.InvitationId
import com.unicorn.server.domain.invitation.vo.InvitationToken
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.port.dto.MemberProfileDto
import com.unicorn.server.domain.member.port.`in`.GetMemberProfileInPort
import com.unicorn.server.domain.member.vo.MemberId
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
@DisplayName("InvitationService 단위 테스트")
class InvitationServiceTest {
	@Test
	@DisplayName("써클 구성원이 아닌 사용자가 초대장을 발급하면 예외가 발생한다")
	fun issue_withUnauthorizedInviter_throwsException() {
		val fixture = Fixture()
		val owner = fixture.member("owner3@example.com", "owner3")
		val outsider = fixture.member("outsider@example.com", "outsider")
		fixture.memberProfileInPort.save(owner)
		fixture.memberProfileInPort.save(outsider)
		val circleId = TestIdFactory.circleId().toString()
		fixture.circleInPort.put(CircleSummary(circleId, "무인가써클", owner.id.toString()))
		fixture.circleMemberInPort.put(circleId, owner.id.toString())

		assertThatThrownBy {
			fixture.service.issue(
				outsider.id.toString(),
				IssueInvitationCommand(InvitationType.CIRCLE, circleId, null, null),
			)
		}
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(InvitationErrorCode.INVITATION_NOT_AUTHORIZED)
	}

	@Test
	@DisplayName("만료된 초대장을 조회하면 예외가 발생한다")
	fun getByToken_withExpiredInvitation_throwsException() {
		val fixture = Fixture()
		val owner = fixture.member("owner4@example.com", "owner4")
		fixture.memberProfileInPort.save(owner)
		val circleId = TestIdFactory.circleId().toString()
		fixture.circleInPort.put(CircleSummary(circleId, "만료초대테스트", owner.id.toString()))

		val expiredInvitation = Invitation(
			id = TestIdFactory.invitationId(),
			type = InvitationType.CIRCLE,
			targetId = circleId,
			token = InvitationToken("abcdefghijklmnopqrstuvwxABCDEFGH"),
			inviterId = owner.id,
			inviteToName = null,
			message = null,
			status = com.unicorn.server.domain.invitation.enums.InvitationStatus.ACTIVE,
			expiresAt = LocalDateTime.now().minusMinutes(1),
			deleted = false,
			createdAt = LocalDateTime.now().minusHours(49),
			updatedAt = LocalDateTime.now().minusHours(49),
		)
		fixture.invitationOutPort.save(expiredInvitation)

		assertThatThrownBy { fixture.service.getByToken(expiredInvitation.token.value) }
			.isInstanceOf(InvitationExpiredException::class.java)
	}

	private class Fixture {
		val invitationOutPort = FakeInvitationOutPort()
		val circleInPort = FakeCircleInPort()
		val circleMemberInPort = FakeCircleMemberInPort()
		val memberProfileInPort = FakeMemberProfileInPort()
		val service = InvitationService(
			invitationOutPort = invitationOutPort,
			invitationIdGenerator = object : InvitationIdGenerator { override fun next() = TestIdFactory.invitationId() },
			tokenGenerator = object : InvitationTokenGenerator {
				override fun generate(): InvitationToken = InvitationToken("abcdefghijklmnopqrstuvwxABCDEFGH")
			},
			circleInPort = circleInPort,
			circleMemberInPort = circleMemberInPort,
			getMemberProfileInPort = memberProfileInPort,
			eventPublisher = object : EventPublisher { override fun publish(event: com.unicorn.server.common.domain.Event) = Unit },
		)

		fun member(email: String, nickname: String): Member = Member.create(TestIdFactory.memberId(), Email(email), nickname, nickname, role = Role.MEMBER)
	}

	private class FakeInvitationOutPort : InvitationOutPort {
		private val invitations = linkedMapOf<InvitationId, Invitation>()
		override fun save(invitation: Invitation): Invitation { invitations[invitation.id] = invitation; return invitation }
		override fun findById(invitationId: InvitationId): Invitation? = invitations[invitationId]
		override fun findByToken(token: InvitationToken): Invitation? = invitations.values.firstOrNull { it.token == token }
	}

	private class FakeCircleInPort : CircleInPort {
		private val circles = linkedMapOf<String, CircleSummary>()
		override fun create(memberId: String, command: CreateCircleCommand): CircleSummary = error("not used")
		override fun getCircleSummary(circleId: String): CircleSummary = circles.getValue(circleId)
		fun put(summary: CircleSummary) { circles[summary.id] = summary }
	}

	private class FakeCircleMemberInPort : CircleMemberInPort {
		private val members = mutableSetOf<Pair<String, String>>()
		override fun join(circleId: String, memberId: String) = error("not used")
		override fun getCircleMembers(circleId: String): List<CircleMemberDto> = emptyList()
		override fun isCircleMember(circleId: String, memberId: String): Boolean = members.contains(circleId to memberId)
		fun put(circleId: String, memberId: String) { members.add(circleId to memberId) }
	}

	private class FakeMemberProfileInPort : GetMemberProfileInPort {
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
}
