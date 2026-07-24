package com.unicorn.server.domain.schedule.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.port.out.event.EventPublisher
import com.unicorn.server.domain.schedule.event.FamilyScheduleNotificationRequestedEvent
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import com.unicorn.server.domain.schedule.port.`in`.RequestFamilyScheduleNotificationInPort
import com.unicorn.server.domain.schedule.port.dto.RequestFamilyScheduleNotificationCommand
import com.unicorn.server.domain.schedule.port.out.CircleAccessOutPort
import com.unicorn.server.domain.schedule.port.out.ScheduleOutPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@Service
@Transactional
class FamilyScheduleNotificationService(
	private val scheduleOutPort: ScheduleOutPort,
	private val circleAccessOutPort: CircleAccessOutPort,
	private val eventPublisher: EventPublisher,
) : RequestFamilyScheduleNotificationInPort {

	override fun request(command: RequestFamilyScheduleNotificationCommand) {
		if (!circleAccessOutPort.isMember(command.circleId, command.memberId)) {
			throw BusinessException(ScheduleErrorCode.CIRCLE_ACCESS_DENIED)
		}

		val schedule = scheduleOutPort.findActiveByIdAndCircleId(command.scheduleId, command.circleId)
			?: throw BusinessException(ScheduleErrorCode.SCHEDULE_NOT_FOUND)
		val dDay = schedule.computeDDay(today())
			?: throw BusinessException(ScheduleErrorCode.FAMILY_SCHEDULE_NOTIFICATION_NOT_AVAILABLE)

		eventPublisher.publish(
			FamilyScheduleNotificationRequestedEvent(
				requestId = UUID.randomUUID().toString(),
				scheduleId = schedule.id.value,
				circleId = schedule.circleId,
				senderMemberId = command.memberId,
				scheduleTitle = schedule.title,
				dDay = dDay.toLabel(),
			),
		)
	}

	private fun Int.toLabel(): String = if (this == 0) "D-day" else "D-$this"

	private fun today(): LocalDate = LocalDate.now(KST)

	companion object {
		private val KST: ZoneId = ZoneId.of("Asia/Seoul")
	}
}
