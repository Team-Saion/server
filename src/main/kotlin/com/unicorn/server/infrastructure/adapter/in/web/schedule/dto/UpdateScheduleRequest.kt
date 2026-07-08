package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import com.fasterxml.jackson.annotation.JsonSetter
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalTime

@Schema(
	description = """
		일정 수정 요청. Partial Update 방식으로 동작합니다.
		포함된 필드만 수정되며, 포함하지 않은 필드는 기존 값이 유지됩니다.
		needConfirm은 항상 포함해야 합니다.
	""",
)
class UpdateScheduleRequest {
	@field:Schema(
		description = "수정할 제목. 1~30자, 공백 전용 불가. 생략하면 기존 제목 유지.",
		example = "수정된 제목",
		nullable = true,
	)
	@field:Size(max = 30)
	var title: String? = null

	@field:Schema(
		description = "수정할 시작일 (yyyy-MM-dd). 생략하면 기존 시작일 유지.",
		example = "2024-08-02",
		nullable = true,
	)
	var startDate: LocalDate? = null

	@field:Schema(
		description = "수정할 종료일 (yyyy-MM-dd). 생략하면 기존 종료일 유지.",
		example = "2024-08-04",
		nullable = true,
	)
	var endDate: LocalDate? = null

	@field:Schema(
		description = """
			수정할 시작시간 (HH:mm).
			명시적으로 null을 전달하면 종일 일정(isAllDay=true)으로 변경됩니다.
			필드 자체를 생략하면 기존 시작시간이 유지됩니다.
		""",
		example = "10:00",
		nullable = true,
	)
	var startTime: LocalTime? = null
		private set

	@get:Schema(hidden = true)
	var startTimeProvided: Boolean = false
		private set

	@field:Schema(
		description = """
			수정할 종료시간 (HH:mm).
			명시적으로 null을 전달하면 종일 일정(isAllDay=true)으로 변경됩니다.
			필드 자체를 생략하면 기존 종료시간이 유지됩니다.
		""",
		example = "19:00",
		nullable = true,
	)
	var endTime: LocalTime? = null
		private set

	@get:Schema(hidden = true)
	var endTimeProvided: Boolean = false
		private set

	@field:Schema(
		description = "확인하기 기능 활성 여부. 항상 포함해야 합니다.",
		example = "false",
	)
	@field:NotNull
	var needConfirm: Boolean? = null

	@field:Schema(
		description = """
			수정할 메모 (최대 500자).
			명시적으로 null을 전달하면 메모가 삭제됩니다.
			필드 자체를 생략하면 기존 메모가 유지됩니다.
		""",
		example = "수정된 메모",
		nullable = true,
	)
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
