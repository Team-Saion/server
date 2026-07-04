package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.schedule.ScheduleConfirmation
import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import com.unicorn.server.domain.schedule.port.`in`.RegisterConfirmationInPort
import com.unicorn.server.domain.schedule.port.dto.RegisterConfirmationCommand
import com.unicorn.server.domain.schedule.port.out.CircleAccessOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleConfirmationOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ScheduleConfirmationService(
	private val scheduleOutPort: ScheduleOutPort,
	private val scheduleConfirmationOutPort: ScheduleConfirmationOutPort,
	private val circleAccessOutPort: CircleAccessOutPort,
) : RegisterConfirmationInPort {

	override fun register(command: RegisterConfirmationCommand): ConfirmationType {
		if (!circleAccessOutPort.isMember(command.circleId, command.memberId)) {
			throw BusinessException(ScheduleErrorCode.CIRCLE_ACCESS_DENIED)
		}

		val schedule = scheduleOutPort.findActiveByIdAndCircleId(command.scheduleId, command.circleId)
			?: throw BusinessException(ScheduleErrorCode.SCHEDULE_NOT_FOUND)
		if (!schedule.needConfirm) {
			throw BusinessException(ScheduleErrorCode.CONFIRMATION_NOT_SUPPORTED)
		}

		val existing = scheduleConfirmationOutPort.findByScheduleIdAndMemberId(command.scheduleId, command.memberId)
		if (existing == null) {
			scheduleConfirmationOutPort.save(
				ScheduleConfirmation.create(
					scheduleId = command.scheduleId,
					memberId = command.memberId,
					confirmationType = command.confirmationType,
					createdBy = command.memberId,
				),
			)
		} else if (existing.confirmationType != command.confirmationType) {
			existing.changeType(command.confirmationType, command.memberId)
			scheduleConfirmationOutPort.save(existing)
		}

		return command.confirmationType
	}
}
