package com.unicorn.server.infrastructure.adapter.`in`.web.schedule

import com.unicorn.server.domain.schedule.port.`in`.CreateScheduleInPort
import com.unicorn.server.domain.schedule.port.`in`.DeleteScheduleInPort
import com.unicorn.server.domain.schedule.port.`in`.GetScheduleDetailInPort
import com.unicorn.server.domain.schedule.port.`in`.GetScheduleListInPort
import com.unicorn.server.domain.schedule.port.`in`.RegisterConfirmationInPort
import com.unicorn.server.domain.schedule.port.`in`.UpdateScheduleInPort
import com.unicorn.server.domain.schedule.port.dto.CreateScheduleCommand
import com.unicorn.server.domain.schedule.port.dto.RegisterConfirmationCommand
import com.unicorn.server.domain.schedule.port.dto.UpdateScheduleCommand
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.CreateScheduleRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.RegisterConfirmationRequest
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.RegisterConfirmationResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.ScheduleDetailResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.ScheduleIdResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.ScheduleListResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.UpdateScheduleRequest
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/circles/{circleId}/schedules")
class ScheduleController(
	private val createScheduleInPort: CreateScheduleInPort,
	private val updateScheduleInPort: UpdateScheduleInPort,
	private val deleteScheduleInPort: DeleteScheduleInPort,
	private val getScheduleListInPort: GetScheduleListInPort,
	private val getScheduleDetailInPort: GetScheduleDetailInPort,
	private val registerConfirmationInPort: RegisterConfirmationInPort,
) : ScheduleApiDoc {

	@PostMapping
	override fun createSchedule(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
		@RequestBody @Valid request: CreateScheduleRequest,
	): ApiResponse<ScheduleIdResponse> {
		val scheduleId = createScheduleInPort.create(
			CreateScheduleCommand(
				memberId = memberId,
				circleId = circleId,
				title = request.title,
				startDate = request.startDate,
				endDate = request.endDate,
				startTime = request.startTime,
				endTime = request.endTime,
				needConfirm = request.needConfirm,
				memo = request.memo,
			),
		)
		return ApiResponse.created(ScheduleIdResponse.of(scheduleId))
	}

	@PatchMapping("/{scheduleId}")
	override fun updateSchedule(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
		@PathVariable scheduleId: Long,
		@RequestBody @Valid request: UpdateScheduleRequest,
	): ApiResponse<Unit> {
		updateScheduleInPort.update(
			UpdateScheduleCommand(
				scheduleId = scheduleId,
				circleId = circleId,
				memberId = memberId,
				title = request.title,
				startDate = request.startDate,
				endDate = request.endDate,
				startTime = request.startTime,
				endTime = request.endTime,
				startTimeProvided = request.startTimeProvided,
				endTimeProvided = request.endTimeProvided,
				needConfirm = request.needConfirm!!,
				memo = request.memo,
				memoProvided = request.memoProvided,
			),
		)
		return ApiResponse.success()
	}

	@DeleteMapping("/{scheduleId}")
	override fun deleteSchedule(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
		@PathVariable scheduleId: Long,
	): ApiResponse<Unit> {
		deleteScheduleInPort.delete(scheduleId, circleId, memberId)
		return ApiResponse.success()
	}

	@GetMapping
	override fun getScheduleList(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
		@RequestParam cursor: String?,
		@RequestParam(defaultValue = "20") size: Int,
	): ApiResponse<ScheduleListResponse> {
		val result = getScheduleListInPort.getList(circleId, memberId, cursor, size)
		return ApiResponse.success(ScheduleListResponse.from(result))
	}

	@GetMapping("/{scheduleId}")
	override fun getScheduleDetail(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
		@PathVariable scheduleId: Long,
	): ApiResponse<ScheduleDetailResponse> {
		val result = getScheduleDetailInPort.getDetail(scheduleId, circleId, memberId)
		return ApiResponse.success(ScheduleDetailResponse.from(result))
	}

	@PostMapping("/{scheduleId}/confirmations")
	override fun registerConfirmation(
		@AuthenticationPrincipal memberId: String,
		@PathVariable circleId: String,
		@PathVariable scheduleId: Long,
		@RequestBody @Valid request: RegisterConfirmationRequest,
	): ApiResponse<RegisterConfirmationResponse> {
		val type = registerConfirmationInPort.register(
			RegisterConfirmationCommand(
				scheduleId = scheduleId,
				circleId = circleId,
				memberId = memberId,
				confirmationType = request.confirmationType,
			),
		)
		return ApiResponse.success(RegisterConfirmationResponse.of(type))
	}
}
