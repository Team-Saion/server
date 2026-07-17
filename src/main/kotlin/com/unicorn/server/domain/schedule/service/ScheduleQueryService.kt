package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.circle.vo.CircleId
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.enums.UrgencyLevel
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import com.unicorn.server.domain.schedule.port.`in`.GetScheduleDetailInPort
import com.unicorn.server.domain.schedule.port.`in`.GetScheduleListInPort
import com.unicorn.server.domain.schedule.port.`in`.GetSchedulesForCircleInPort
import com.unicorn.server.domain.schedule.port.dto.MyConfirmationInfo
import com.unicorn.server.domain.schedule.port.dto.ScheduleDetailResult
import com.unicorn.server.domain.schedule.port.dto.ScheduleListResult
import com.unicorn.server.domain.schedule.port.dto.SchedulePageCursor
import com.unicorn.server.domain.schedule.port.dto.ScheduleSummaryResult
import com.unicorn.server.domain.schedule.port.out.CircleAccessOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleConfirmationOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Service
@Transactional(readOnly = true)
class ScheduleQueryService(
	private val scheduleOutPort: ScheduleOutPort,
	private val scheduleConfirmationOutPort: ScheduleConfirmationOutPort,
	private val circleAccessOutPort: CircleAccessOutPort,
) : GetScheduleListInPort, GetScheduleDetailInPort, GetSchedulesForCircleInPort {

	override fun getList(
		circleId: String,
		memberId: String,
		cursor: String?,
		size: Int,
	): ScheduleListResult {
		if (!circleAccessOutPort.isMember(circleId, memberId)) {
			throw BusinessException(ScheduleErrorCode.CIRCLE_ACCESS_DENIED)
		}

		if (size !in MIN_PAGE_SIZE..MAX_PAGE_SIZE) {
			throw BusinessException(CommonErrorCode.INVALID_INPUT, "size=$size")
		}
		val pageSize = size
		val pageCursor = cursor?.takeIf { it.isNotBlank() }?.let { decodeCursor(it) }
		val results = scheduleOutPort.findActiveByCircleId(circleId, pageCursor, pageSize + 1)
		val hasNext = results.size > pageSize
		val schedules = results.take(pageSize)
		val nextCursor = if (hasNext && schedules.isNotEmpty()) {
			SchedulePageCursor.from(schedules.last()).encode()
		} else {
			null
		}

		return ScheduleListResult(
			schedules = schedules.map { it.toSummaryResult() },
			nextCursor = nextCursor,
			hasNext = hasNext,
		)
	}

	override fun getDetail(scheduleId: ScheduleId, circleId: String, memberId: String): ScheduleDetailResult {
		if (!circleAccessOutPort.isMember(circleId, memberId)) {
			throw BusinessException(ScheduleErrorCode.CIRCLE_ACCESS_DENIED)
		}

		val schedule = scheduleOutPort.findActiveByIdAndCircleId(scheduleId, circleId)
			?: throw BusinessException(ScheduleErrorCode.SCHEDULE_NOT_FOUND)
		val confirmations = if (schedule.needConfirm) {
			scheduleConfirmationOutPort.countGroupByType(scheduleId)
		} else {
			emptyList()
		}
		val myConfirmation = if (schedule.needConfirm) {
			scheduleConfirmationOutPort.findByScheduleIdAndMemberId(scheduleId, memberId)
				?.let { MyConfirmationInfo(it.id, it.confirmationType) }
		} else {
			null
		}

		return ScheduleDetailResult(
			scheduleId = schedule.id,
			title = schedule.title,
			startDate = schedule.startDate,
			endDate = schedule.endDate,
			startTime = schedule.startTime,
			endTime = schedule.endTime,
			isAllDay = schedule.isAllDay,
			needConfirm = schedule.needConfirm,
			memo = schedule.memo,
			status = schedule.computeStatus(nowDateTime()),
			dDay = schedule.computeDDay(today()),
			urgencyLevel = UrgencyLevel.from(schedule.computeDDay(today())),
			progressRate = schedule.computeProgressRate(nowDateTime()),
			confirmations = confirmations,
			myConfirmation = myConfirmation,
			createdBy = schedule.createdBy,
			createdAt = schedule.createdAt,
		)
	}

	override fun findUpcomingSchedulesByCircleId(
		circleId: CircleId,
		today: LocalDate,
		limit: Int,
	): List<ScheduleSummaryResult> =
		scheduleOutPort.findUpcomingByCircleId(circleId.value, today, limit)
			.map { it.toSummaryResult() }

	override fun countByCircleId(circleId: CircleId): Long =
		scheduleOutPort.countActiveByCircleId(circleId.value)

	private fun Schedule.toSummaryResult(): ScheduleSummaryResult {
		val dDay = computeDDay(today())
		return ScheduleSummaryResult(
			scheduleId = id,
			title = title,
			startDate = startDate,
			endDate = endDate,
			startTime = startTime,
			endTime = endTime,
			isAllDay = isAllDay,
			needConfirm = needConfirm,
			status = computeStatus(nowDateTime()),
			dDay = dDay,
			urgencyLevel = UrgencyLevel.from(dDay),
			progressRate = computeProgressRate(nowDateTime()),
		)
	}

	private fun decodeCursor(cursor: String): SchedulePageCursor =
		try {
			SchedulePageCursor.decode(cursor)
		} catch (exception: IllegalArgumentException) {
			throw BusinessException(CommonErrorCode.INVALID_INPUT, "cursor=$cursor", exception)
		}

	private fun nowDateTime(): LocalDateTime = LocalDateTime.now(KST)

	private fun today(): LocalDate = LocalDate.now(KST)

	companion object {
		private val KST: ZoneId = ZoneId.of("Asia/Seoul")
		private const val MIN_PAGE_SIZE = 1
		private const val MAX_PAGE_SIZE = 50
	}
}
