package com.unicorn.server.domain.home.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.circle.exception.CircleErrorCode
import com.unicorn.server.domain.circle.port.`in`.CircleInPort
import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.home.port.dto.HomeCircleDto
import com.unicorn.server.domain.home.port.dto.HomeMemberDto
import com.unicorn.server.domain.home.port.dto.HomeView
import com.unicorn.server.domain.home.port.`in`.HomeQueryInPort
import com.unicorn.server.domain.member.port.`in`.GetMemberProfileInPort
import com.unicorn.server.domain.schedule.port.`in`.GetSchedulesForCircleInPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class HomeQueryService(
    private val circleInPort: CircleInPort,
    private val circleMemberInPort: CircleMemberInPort,
    private val getMemberProfileInPort: GetMemberProfileInPort,
    private val scheduleQueryInPort: GetSchedulesForCircleInPort,
) : HomeQueryInPort {
    override fun getHome(circleId: String, requesterId: String): HomeView {
        val circle = circleInPort.getCircleSummary(circleId)
        if (!circleMemberInPort.isCircleMember(circleId, requesterId)) {
            throw BusinessException(CircleErrorCode.CIRCLE_ACCESS_DENIED)
        }
        val visibleMembers = getVisibleMembers(circleId, requesterId)

        val today = LocalDate.now()
        val circleIdVo = CircleId.of(circleId)
        // 메인(가장 임박한 1건) + 나머지 최대 3건을 한 번의 조회로 가져와 분리한다.
        val upcomingSchedules = scheduleQueryInPort.findUpcomingSchedulesByCircleId(circleIdVo, today, MAIN_AND_UPCOMING_LIMIT)
        return HomeView(
            circle = HomeCircleDto(circle.id, circle.name, circle.ownerId),
            members = visibleMembers,
            canInvite = visibleMembers.size == 1,
            mainSchedule = upcomingSchedules.firstOrNull(),
            schedules = upcomingSchedules.drop(1),
            totalScheduleCount = scheduleQueryInPort.countByCircleId(circleIdVo),
        )
    }

    override fun getMembers(circleId: String, requesterId: String): List<HomeMemberDto> {
        circleInPort.getCircleSummary(circleId)
        if (!circleMemberInPort.isCircleMember(circleId, requesterId)) {
            throw BusinessException(CircleErrorCode.CIRCLE_ACCESS_DENIED)
        }
        return getVisibleMembers(circleId, requesterId)
    }

    private fun getVisibleMembers(circleId: String, requesterId: String): List<HomeMemberDto> =
        circleMemberInPort.getCircleMembers(circleId)
            .filter { it.active }
            .mapNotNull { membership ->
                val profile = getMemberProfileInPort.getMemberProfile(membership.memberId) ?: return@mapNotNull null
                if (!profile.active) return@mapNotNull null
                HomeMemberDto(
                    memberId = profile.memberId,
                    nickname = membership.nickname,
                    avatarColor = profile.avatarColor,
                    profileImageKey = profile.profileImageKey,
                    isMe = profile.memberId == requesterId,
                    role = membership.role,
                )
            }

    companion object {
        // 메인 일정 1건 + 홈에 노출할 나머지 일정 3건
        private const val MAIN_AND_UPCOMING_LIMIT = 4
    }
}
