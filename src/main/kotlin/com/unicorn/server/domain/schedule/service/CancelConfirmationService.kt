package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import com.unicorn.server.domain.schedule.port.`in`.CancelConfirmationInPort
import com.unicorn.server.domain.schedule.port.out.CircleAccessOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleConfirmationOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CancelConfirmationService(
	private val scheduleOutPort: ScheduleOutPort,
	private val scheduleConfirmationOutPort: ScheduleConfirmationOutPort,
	private val circleAccessOutPort: CircleAccessOutPort,
) : CancelConfirmationInPort {

	override fun cancel(confirmationId: Long, scheduleId: ScheduleId, circleId: String, memberId: String) {
		if (!circleAccessOutPort.isMember(circleId, memberId)) {
			throw BusinessException(ScheduleErrorCode.CIRCLE_ACCESS_DENIED)
		}

		scheduleOutPort.findActiveByIdAndCircleId(scheduleId, circleId)
			?: throw BusinessException(ScheduleErrorCode.SCHEDULE_NOT_FOUND)

		val confirmation = scheduleConfirmationOutPort.findById(confirmationId)
			?: throw BusinessException(ScheduleErrorCode.CONFIRMATION_NOT_FOUND)
		if (confirmation.scheduleId != scheduleId || confirmation.memberId != memberId) {
			throw BusinessException(ScheduleErrorCode.CONFIRMATION_ACCESS_DENIED)
		}

		scheduleConfirmationOutPort.deleteById(confirmationId)
	}
}
