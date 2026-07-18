package com.unicorn.server.domain.home

import com.unicorn.server.TestIdFactory
import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.domain.circle.port.dto.CircleMemberDto
import com.unicorn.server.domain.circle.port.dto.CircleSummary
import com.unicorn.server.domain.circle.port.dto.CreateCircleCommand
import com.unicorn.server.domain.circle.port.`in`.CircleInPort
import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.member.port.dto.MemberProfileDto
import com.unicorn.server.domain.member.port.`in`.GetMemberProfileInPort
import com.unicorn.server.domain.member.enums.AvatarColor
import com.unicorn.server.domain.home.service.HomeQueryService
import com.unicorn.server.domain.schedule.port.`in`.GetSchedulesForCircleInPort
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

@DisplayName("HomeQueryService 단위 테스트")
class HomeQueryServiceTest {
	private val circleInPort = FakeCircleInPort()
	private val circleMemberInPort = FakeCircleMemberInPort()
	private val memberProfileInPort = FakeMemberProfileInPort()
	private val scheduleQueryInPort = FakeScheduleQueryInPort()
	private val homeQueryService = HomeQueryService(circleInPort, circleMemberInPort, memberProfileInPort, scheduleQueryInPort)

	@Test
	@DisplayName("조회 요청자가 홈에 노출 가능한 구성원이 아니면 접근 거부된다")
	fun getHome_requesterNotVisible_throwsException() {
		val circleId = TestIdFactory.circleId().toString()
		val ownerId = java.util.UUID.randomUUID().toString()
		val hiddenId = java.util.UUID.randomUUID().toString()
		circleInPort.put(CircleSummary(circleId, "홈테스트1", ownerId))
		circleMemberInPort.put(circleId, CircleMemberDto(ownerId, "owner1", "INITIATOR", true))
		memberProfileInPort.put(ownerId, active = true)
		memberProfileInPort.put(hiddenId, active = false)

		assertThatThrownBy { homeQueryService.getHome(circleId, hiddenId) }
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(CircleErrorCode.CIRCLE_ACCESS_DENIED)
	}

	@Test
	@DisplayName("비활성 멤버 프로필은 홈 구성원 목록에서 제외된다")
	fun getHome_inactiveProfile_hidden() {
		val circleId = TestIdFactory.circleId().toString()
		val ownerId = java.util.UUID.randomUUID().toString()
		val inactiveId = java.util.UUID.randomUUID().toString()
		circleInPort.put(CircleSummary(circleId, "홈테스트2", ownerId))
		circleMemberInPort.put(circleId, CircleMemberDto(ownerId, "owner2", "INITIATOR", true))
		circleMemberInPort.put(circleId, CircleMemberDto(inactiveId, "inactive2", "MEMBER", true))
		memberProfileInPort.put(ownerId, active = true)
		memberProfileInPort.put(inactiveId, active = false)

		val home = homeQueryService.getHome(circleId, ownerId)

		assertThat(home.members).hasSize(1)
		assertThat(home.members.single().memberId).isEqualTo(ownerId)
		assertThat(home.canInvite).isTrue()
	}

	@Test
	@DisplayName("getMembers는 스케줄 조회 없이 구성원 목록만 반환한다")
	fun getMembers_withoutScheduleQueries_success() {
		val circleId = TestIdFactory.circleId().toString()
		val ownerId = java.util.UUID.randomUUID().toString()
		val friendId = java.util.UUID.randomUUID().toString()
		circleInPort.put(CircleSummary(circleId, "홈테스트3", ownerId))
		circleMemberInPort.put(circleId, CircleMemberDto(ownerId, "owner3", "INITIATOR", true))
		circleMemberInPort.put(circleId, CircleMemberDto(friendId, "friend3", "MEMBER", true))
		memberProfileInPort.put(ownerId, active = true)
		memberProfileInPort.put(friendId, active = true, profileImageKey = "images/profile/friend3.png")

		val members = homeQueryService.getMembers(circleId, ownerId)

		assertThat(members).hasSize(2)
		assertThat(members.first { it.memberId == friendId }.profileImageKey).isEqualTo("images/profile/friend3.png")
		assertThat(members.first { it.memberId == friendId }.avatarColor).isEqualTo(AvatarColor.TEAL_200)
		assertThat(scheduleQueryInPort.listCalled).isFalse()
		assertThat(scheduleQueryInPort.countCalled).isFalse()
	}

	private class FakeCircleInPort : CircleInPort {
		private val circles = linkedMapOf<String, CircleSummary>()
		override fun create(memberId: String, command: CreateCircleCommand): CircleSummary = error("not used")
		override fun listCircles(memberId: String): List<CircleSummary> = error("not used")
		override fun getCircleSummary(circleId: String): CircleSummary = circles.getValue(circleId)
		fun put(summary: CircleSummary) { circles[summary.id] = summary }
	}

	private class FakeCircleMemberInPort : CircleMemberInPort {
		private val memberships = linkedMapOf<String, MutableList<CircleMemberDto>>()
		override fun getCircleMembers(circleId: String): List<CircleMemberDto> = memberships[circleId].orEmpty()
		override fun join(circleId: String, memberId: String) = error("not used")
		override fun leave(circleId: String, memberId: String) = error("not used")
		override fun handleMemberWithdrawal(memberId: String) = error("not used")
		override fun isCircleMember(circleId: String, memberId: String): Boolean =
			getCircleMembers(circleId).any { it.memberId == memberId && it.active }
		override fun transferInitiator(circleId: String, currentInitiatorId: String, newInitiatorId: String) = error("not used")
		fun put(circleId: String, dto: CircleMemberDto) { memberships.computeIfAbsent(circleId) { mutableListOf() }.add(dto) }
	}

	private class FakeMemberProfileInPort : GetMemberProfileInPort {
		private val profiles = linkedMapOf<String, MemberProfileDto>()
		override fun getMemberProfile(memberId: String): MemberProfileDto? = profiles[memberId]
		fun put(memberId: String, active: Boolean, profileImageKey: String? = null) {
			profiles[memberId] = MemberProfileDto(
				memberId = memberId,
				nickname = "nick-${memberId.take(4)}",
				avatarColor = AvatarColor.TEAL_200,
				profileImageKey = profileImageKey,
				kakaoNickname = null,
				active = active,
			)
		}
	}

	private class FakeScheduleQueryInPort : GetSchedulesForCircleInPort {
		var listCalled = false
		var countCalled = false

		override fun findUpcomingSchedulesByCircleId(circleId: CircleId, today: LocalDate, limit: Int) = emptyList<com.unicorn.server.domain.schedule.port.dto.ScheduleSummaryResult>().also { listCalled = true }
		override fun countByCircleId(circleId: CircleId): Long = 0L.also { countCalled = true }
	}
}
