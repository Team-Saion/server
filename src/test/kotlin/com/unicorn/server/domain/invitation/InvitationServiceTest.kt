package com.unicorn.server.domain.invitation

import com.unicorn.server.TestIdFactory
import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.domain.Event
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.circle.port.dto.CircleMemberDto
import com.unicorn.server.domain.circle.port.dto.CircleSummary
import com.unicorn.server.domain.circle.port.dto.CreateCircleCommand
import com.unicorn.server.domain.circle.port.`in`.CircleInPort
import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.domain.invitation.enums.InvitationChannel
import com.unicorn.server.domain.invitation.enums.InvitationType
import com.unicorn.server.domain.invitation.event.InvitationRedeemedEvent
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
import org.assertj.core.api.Assertions.assertThat
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
				IssueInvitationCommand(circleId),
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

	@Test
	@DisplayName("구성원이 10명인 써클은 초대장을 발급할 수 없다")
	fun issue_withFullCircle_throwsException() {
		val fixture = Fixture()
		val owner = fixture.member("owner5@example.com", "owner5")
		fixture.memberProfileInPort.save(owner)
		val circleId = TestIdFactory.circleId().toString()
		fixture.circleInPort.put(CircleSummary(circleId, "만원써클", owner.id.toString()))
		fixture.circleMemberInPort.put(circleId, owner.id.toString())
		repeat(9) { index ->
			fixture.circleMemberInPort.put(circleId, "member-$index")
		}

		assertThatThrownBy {
			fixture.service.issue(
				owner.id.toString(),
				IssueInvitationCommand(circleId),
			)
		}
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.CIRCLE_MEMBER_LIMIT_EXCEEDED)
	}

	@Test
	@DisplayName("같은 발급자가 초대장을 재발급하면 이전 ACTIVE 초대장은 즉시 만료된다")
	fun issue_reissue_expiresPreviousInvitation() {
		val fixture = Fixture()
		val owner = fixture.member("owner6@example.com", "owner6")
		fixture.memberProfileInPort.save(owner)
		val circleId = TestIdFactory.circleId().toString()
		fixture.circleInPort.put(CircleSummary(circleId, "재발급써클", owner.id.toString()))
		fixture.circleMemberInPort.put(circleId, owner.id.toString())

		val firstIssued = fixture.service.issue(
			owner.id.toString(),
			IssueInvitationCommand(circleId),
		)
		val secondIssued = fixture.service.issue(
			owner.id.toString(),
			IssueInvitationCommand(circleId),
		)

		assertThat(fixture.invitationOutPort.findById(InvitationId.of(firstIssued.invitationId))?.status)
			.isEqualTo(com.unicorn.server.domain.invitation.enums.InvitationStatus.EXPIRED)
		assertThat(fixture.invitationOutPort.findById(InvitationId.of(secondIssued.invitationId))?.status)
			.isEqualTo(com.unicorn.server.domain.invitation.enums.InvitationStatus.ACTIVE)
		assertThat(fixture.invitationOutPort.findById(InvitationId.of(secondIssued.invitationId))?.type)
			.isEqualTo(InvitationType.CIRCLE)
	}

	@Test
	@DisplayName("초대 수락 시 후속 알림용 정보가 포함된 이벤트를 발행한다")
	fun accept_publishesRedeemedEventWithNotificationPayload() {
		val fixture = Fixture()
		val owner = fixture.member("owner7@example.com", "owner7")
		val invitee = fixture.member("invitee7@example.com", "invitee7")
		fixture.memberProfileInPort.save(owner)
		fixture.memberProfileInPort.save(invitee)
		val circleId = TestIdFactory.circleId().toString()
		fixture.circleInPort.put(CircleSummary(circleId, "알림써클", owner.id.toString()))
		fixture.circleMemberInPort.put(circleId, owner.id.toString())
		fixture.circleMemberInPort.joinResult = com.unicorn.server.domain.circle.port.dto.JoinCircleResult(circleId)

		val invitation = Invitation.create(
			id = TestIdFactory.invitationId(),
			type = InvitationType.CIRCLE,
			targetId = circleId,
			token = InvitationToken("abcdefghijklmnopqrstuvwxABCDEFGH"),
			inviterId = owner.id,
		)
		fixture.invitationOutPort.save(invitation)

		fixture.service.accept(invitation.token.value, invitee.id.toString())

		val event = fixture.eventPublisher.events.filterIsInstance<InvitationRedeemedEvent>().single()
		assertThat(event.inviterMemberId).isEqualTo(owner.id.toString())
		assertThat(event.redeemerMemberId).isEqualTo(invitee.id.toString())
		assertThat(event.circleName).isEqualTo("알림써클")
		assertThat(event.redeemerNickname).isEqualTo(invitee.nickname)
	}

	private class Fixture {
		val invitationOutPort = FakeInvitationOutPort()
		val circleInPort = FakeCircleInPort()
		val circleMemberInPort = FakeCircleMemberInPort()
		val memberProfileInPort = FakeMemberProfileInPort()
		val eventPublisher = RecordingEventPublisher()
		val service = InvitationService(
			invitationOutPort = invitationOutPort,
			invitationIdGenerator = object : InvitationIdGenerator { override fun next() = TestIdFactory.invitationId() },
			tokenGenerator = object : InvitationTokenGenerator {
				override fun generate(): InvitationToken = InvitationToken("abcdefghijklmnopqrstuvwxABCDEFGH")
			},
			circleInPort = circleInPort,
			circleMemberInPort = circleMemberInPort,
			getMemberProfileInPort = memberProfileInPort,
			eventPublisher = eventPublisher,
		)

		fun member(email: String, nickname: String): Member = Member.create(Email(email), nickname, nickname, role = Role.MEMBER)
	}

	private class FakeInvitationOutPort : InvitationOutPort {
		private val invitations = linkedMapOf<InvitationId, Invitation>()
		override fun save(invitation: Invitation): Invitation { invitations[invitation.id] = invitation; return invitation }
		override fun findById(invitationId: InvitationId): Invitation? = invitations[invitationId]
		override fun findByToken(token: InvitationToken): Invitation? = invitations.values.firstOrNull { it.token == token }
		override fun findAllActiveByTypeAndTargetIdAndInviterId(type: InvitationType, targetId: String, inviterId: MemberId): List<Invitation> =
			invitations.values.filter { it.type == type && it.targetId == targetId && it.inviterId == inviterId && it.status == com.unicorn.server.domain.invitation.enums.InvitationStatus.ACTIVE && !it.deleted }
	}

	private class FakeCircleInPort : CircleInPort {
		private val circles = linkedMapOf<String, CircleSummary>()
		override fun create(memberId: String, command: CreateCircleCommand): CircleSummary = error("not used")
		override fun listCircles(memberId: String): List<CircleSummary> = error("not used")
		override fun getCircleSummary(circleId: String): CircleSummary = circles.getValue(circleId)
		fun put(summary: CircleSummary) { circles[summary.id] = summary }
	}

	private class FakeCircleMemberInPort : CircleMemberInPort {
		private val members = mutableSetOf<Pair<String, String>>()
		var joinResult = com.unicorn.server.domain.circle.port.dto.JoinCircleResult("circle-id")
		override fun join(circleId: String, memberId: String) = joinResult
		override fun leave(circleId: String, memberId: String) = error("not used")
		override fun handleMemberWithdrawal(memberId: String) = error("not used")
		override fun getCircleMembers(circleId: String): List<CircleMemberDto> =
			members.filter { it.first == circleId }.map {
				CircleMemberDto(memberId = it.second, nickname = it.second, role = "MEMBER", active = true)
			}
		override fun isCircleMember(circleId: String, memberId: String): Boolean = members.contains(circleId to memberId)
		override fun transferInitiator(circleId: String, currentInitiatorId: String, newInitiatorId: String) = error("not used")
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
					avatarColor = it.avatarColor,
					profileImageKey = it.profileImageKey,
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
