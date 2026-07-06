package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.schedule.Schedule
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import com.unicorn.server.domain.schedule.port.`in`.CreateScheduleInPort
import com.unicorn.server.domain.schedule.port.`in`.DeleteScheduleInPort
import com.unicorn.server.domain.schedule.port.`in`.UpdateScheduleInPort
import com.unicorn.server.domain.schedule.port.dto.CreateScheduleCommand
import com.unicorn.server.domain.schedule.port.dto.UpdateScheduleCommand
import com.unicorn.server.domain.schedule.port.out.CircleAccessOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleConfirmationOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleIdGenerator
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ScheduleCommandService(
	private val scheduleOutPort: ScheduleOutPort,
	private val scheduleConfirmationOutPort: ScheduleConfirmationOutPort,
	private val circleAccessOutPort: CircleAccessOutPort,
	private val scheduleIdGenerator: ScheduleIdGenerator,
) : CreateScheduleInPort, UpdateScheduleInPort, DeleteScheduleInPort {

	override fun create(command: CreateScheduleCommand): ScheduleId {
		if (!circleAccessOutPort.existsById(command.circleId)) {
			throw BusinessException(ScheduleErrorCode.CIRCLE_NOT_FOUND)
		}
		if (!circleAccessOutPort.isMember(command.circleId, command.memberId)) {
			throw BusinessException(ScheduleErrorCode.CIRCLE_ACCESS_DENIED)
		}

		val scheduleId = scheduleIdGenerator.next()
		val schedule = Schedule.create(
			id = scheduleId,
			circleId = command.circleId,
			title = command.title,
			startDate = command.startDate,
			endDate = command.endDate,
			startTime = command.startTime,
			endTime = command.endTime,
			needConfirm = command.needConfirm,
			memo = command.memo,
			createdBy = command.memberId,
		)

		return scheduleOutPort.save(schedule).id
	}

	override fun update(command: UpdateScheduleCommand) {
		if (!circleAccessOutPort.existsById(command.circleId)) {
			throw BusinessException(ScheduleErrorCode.CIRCLE_NOT_FOUND)
		}
		if (!circleAccessOutPort.isMember(command.circleId, command.memberId)) {
			throw BusinessException(ScheduleErrorCode.CIRCLE_ACCESS_DENIED)
		}

		val schedule = scheduleOutPort.findActiveByIdAndCircleId(command.scheduleId, command.circleId)
			?: throw BusinessException(ScheduleErrorCode.SCHEDULE_NOT_FOUND)
		if (!canModify(schedule.createdBy, command.circleId, command.memberId)) {
			throw BusinessException(ScheduleErrorCode.SCHEDULE_MODIFICATION_DENIED)
		}

		schedule.update(
			title = command.title,
			startDate = command.startDate,
			endDate = command.endDate,
			startTime = command.startTime,
			endTime = command.endTime,
			startTimeProvided = command.startTimeProvided,
			endTimeProvided = command.endTimeProvided,
			needConfirm = command.needConfirm,
			memo = command.memo,
			memoProvided = command.memoProvided,
			updatedBy = command.memberId,
		)
		scheduleOutPort.save(schedule)
	}

	override fun delete(scheduleId: ScheduleId, circleId: String, memberId: String) {
		if (!circleAccessOutPort.existsById(circleId)) {
			throw BusinessException(ScheduleErrorCode.CIRCLE_NOT_FOUND)
		}
		if (!circleAccessOutPort.isMember(circleId, memberId)) {
			throw BusinessException(ScheduleErrorCode.CIRCLE_ACCESS_DENIED)
		}

		val schedule = scheduleOutPort.findActiveByIdAndCircleId(scheduleId, circleId)
			?: throw BusinessException(ScheduleErrorCode.SCHEDULE_NOT_FOUND)
		if (!canModify(schedule.createdBy, circleId, memberId)) {
			throw BusinessException(ScheduleErrorCode.SCHEDULE_MODIFICATION_DENIED)
		}

		schedule.delete(memberId)
		scheduleOutPort.save(schedule)
		scheduleConfirmationOutPort.deleteAllByScheduleId(scheduleId)
	}

	private fun canModify(createdBy: String, circleId: String, memberId: String): Boolean =
		createdBy == memberId || circleAccessOutPort.isInitiator(circleId, memberId)
}
