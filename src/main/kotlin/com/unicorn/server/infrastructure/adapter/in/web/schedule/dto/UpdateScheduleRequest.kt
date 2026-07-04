package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.fasterxml.jackson.annotation.JsonSetter
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalTime

@Schema(description = "일정 수정 요청. 포함된 필드만 수정되며 needConfirm은 항상 포함해야 한다.")
class UpdateScheduleRequest {
	@field:Schema(description = "수정할 제목 (최대 30자)", example = "수정된 제목", nullable = true)
	@field:Size(max = 30)
	var title: String? = null

	@field:Schema(description = "수정할 시작일 (yyyy-MM-dd)", example = "2024-08-02", nullable = true)
	var startDate: LocalDate? = null

	@field:Schema(description = "수정할 종료일 (yyyy-MM-dd)", example = "2024-08-04", nullable = true)
	var endDate: LocalDate? = null

	@field:Schema(description = "수정할 시작시간 (HH:mm). null 전달 시 종일 일정으로 변경.", example = "10:00", nullable = true)
	var startTime: LocalTime? = null
		private set

	@get:Schema(hidden = true)
	var startTimeProvided: Boolean = false
		private set

	@field:Schema(description = "수정할 종료시간 (HH:mm). null 전달 시 종일 일정으로 변경.", example = "19:00", nullable = true)
	var endTime: LocalTime? = null
		private set

	@get:Schema(hidden = true)
	var endTimeProvided: Boolean = false
		private set

	@field:Schema(description = "확인하기 기능 활성 여부", example = "false")
	@field:NotNull
	var needConfirm: Boolean? = null

	@field:Schema(description = "수정할 메모 (최대 500자)", example = "수정된 메모", nullable = true)
	@field:Size(max = 500)
	var memo: String? = null
		private set

	@get:Schema(hidden = true)
	var memoProvided: Boolean = false
		private set

	@JsonSetter("startTime")
	fun updateStartTime(startTime: LocalTime?) {
		this.startTime = startTime
		this.startTimeProvided = true
	}

	@JsonSetter("endTime")
	fun updateEndTime(endTime: LocalTime?) {
		this.endTime = endTime
		this.endTimeProvided = true
	}

	@JsonSetter("memo")
	fun updateMemo(memo: String?) {
		this.memo = memo
		this.memoProvided = true
	}
}
