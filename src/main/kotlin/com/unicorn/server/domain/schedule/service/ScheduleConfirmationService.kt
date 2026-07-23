package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.schedule.ScheduleConfirmation
import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.event.ScheduleConfirmedEvent
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import com.unicorn.server.domain.schedule.port.`in`.RegisterConfirmationInPort
import com.unicorn.server.domain.schedule.port.`in`.ScheduleConfirmationStatusInPort
import com.unicorn.server.domain.schedule.port.dto.RegisterConfirmationCommand
import com.unicorn.server.domain.schedule.port.out.CircleAccessOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleConfirmationOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ScheduleConfirmationService(
	private val scheduleOutPort: ScheduleOutPort,
	private val scheduleConfirmationOutPort: ScheduleConfirmationOutPort,
	private val circleAccessOutPort: CircleAccessOutPort,
	private val eventPublisher: EventPublisher,
) : RegisterConfirmationInPort, ScheduleConfirmationStatusInPort {

	override fun register(command: RegisterConfirmationCommand): ConfirmationType {
		if (!command.confirmationType.available) {
			throw BusinessException(ScheduleErrorCode.INVALID_CONFIRMATION_TYPE)
		}
		if (!circleAccessOutPort.isMember(command.circleId, command.memberId)) {
			throw BusinessException(ScheduleErrorCode.CIRCLE_ACCESS_DENIED)
		}

		val schedule = scheduleOutPort.findActiveByIdAndCircleId(command.scheduleId, command.circleId)
			?: throw BusinessException(ScheduleErrorCode.SCHEDULE_NOT_FOUND)
		if (!schedule.needConfirm) {
			throw BusinessException(ScheduleErrorCode.CONFIRMATION_NOT_SUPPORTED)
		}

		val existing = scheduleConfirmationOutPort.findByScheduleIdAndMemberId(command.scheduleId, command.memberId)
		val wasConfirmed = existing?.confirmationType == ConfirmationType.CONFIRMED
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
		if (command.confirmationType == ConfirmationType.CONFIRMED && !wasConfirmed) {
			eventPublisher.publish(
				ScheduleConfirmedEvent(
					scheduleId = schedule.id.value,
					circleId = schedule.circleId,
					scheduleCreatorMemberId = schedule.createdBy,
					confirmerMemberId = command.memberId,
					scheduleTitle = schedule.title,
				),
			)
		}

		return command.confirmationType
	}

	override fun hasConfirmed(scheduleId: ScheduleId, memberId: String): Boolean =
		scheduleConfirmationOutPort.findByScheduleIdAndMemberId(scheduleId, memberId)
			?.confirmationType == ConfirmationType.CONFIRMED
}
