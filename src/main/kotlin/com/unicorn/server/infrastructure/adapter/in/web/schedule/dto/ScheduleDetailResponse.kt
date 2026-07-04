package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.enums.ScheduleStatus
import com.unicorn.server.domain.schedule.port.dto.ScheduleDetailResult
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@Schema(description = "일정 상세 응답")
data class ScheduleDetailResponse(
	@field:Schema(description = "일정 ID", example = "1")
	val scheduleId: Long,

	@field:Schema(description = "일정 제목", example = "제주도 여행")
	val title: String,

	@field:Schema(description = "시작일 (yyyy-MM-dd)", example = "2024-08-01")
	val startDate: LocalDate,

	@field:Schema(description = "종료일 (yyyy-MM-dd)", example = "2024-08-03")
	val endDate: LocalDate,

	@field:Schema(description = "시작시간 (HH:mm). 종일 일정이면 null.", example = "09:00", nullable = true)
	val startTime: LocalTime?,

	@field:Schema(description = "종료시간 (HH:mm). 종일 일정이면 null.", example = "18:00", nullable = true)
	val endTime: LocalTime?,

	@field:Schema(description = "종일 일정 여부", example = "false")
	val isAllDay: Boolean,

	@field:Schema(description = "확인하기 기능 활성 여부", example = "true")
	val needConfirm: Boolean,

	@field:Schema(description = "일정 상태", example = "UPCOMING", allowableValues = ["UPCOMING", "IN_PROGRESS", "COMPLETED"])
	val status: ScheduleStatus,

	@field:Schema(description = "D-Day 값. 과거 또는 진행 중 기간 일정은 null.", example = "28", nullable = true)
	val dDay: Int?,

	@field:Schema(description = "진행률 (0~100)", example = "0")
	val progressRate: Int,

	@field:Schema(description = "메모. 없으면 null.", nullable = true)
	val memo: String?,

	@field:Schema(description = "확인하기 종류별 카운트 목록. needConfirm=false이면 빈 배열.")
	val confirmations: List<ConfirmationCountResponse>,

	@field:Schema(description = "내가 선택한 확인하기 종류. 미등록 또는 needConfirm=false이면 null.", nullable = true)
	val myConfirmationType: ConfirmationType?,

	@field:Schema(description = "작성자 memberId", example = "123")
	val createdBy: Long,

	@field:Schema(description = "생성 일시", example = "2024-07-01T10:00:00")
	val createdAt: LocalDateTime,
) {
	companion object {
		fun from(result: ScheduleDetailResult) = ScheduleDetailResponse(
			scheduleId = result.scheduleId,
			title = result.title,
			startDate = result.startDate,
			endDate = result.endDate,
			startTime = result.startTime,
			endTime = result.endTime,
			isAllDay = result.isAllDay,
			needConfirm = result.needConfirm,
			status = result.status,
			dDay = result.dDay,
			progressRate = result.progressRate,
			memo = result.memo,
			confirmations = result.confirmations.map { ConfirmationCountResponse.from(it) },
			myConfirmationType = result.myConfirmationType,
			createdBy = result.createdBy,
			createdAt = result.createdAt,
		)
	}
}
