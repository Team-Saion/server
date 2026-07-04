package com.unicorn.server.domain.schedule.service

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.port.`in`.CreateScheduleInPort
import com.unicorn.server.domain.schedule.port.`in`.DeleteScheduleInPort
import com.unicorn.server.domain.schedule.port.`in`.GetScheduleDetailInPort
import com.unicorn.server.domain.schedule.port.`in`.GetScheduleListInPort
import com.unicorn.server.domain.schedule.port.`in`.RegisterConfirmationInPort
import com.unicorn.server.domain.schedule.port.`in`.UpdateScheduleInPort
import com.unicorn.server.domain.schedule.port.dto.CreateScheduleCommand
import com.unicorn.server.domain.schedule.port.dto.RegisterConfirmationCommand
import com.unicorn.server.domain.schedule.port.dto.ScheduleDetailResult
import com.unicorn.server.domain.schedule.port.dto.ScheduleListResult
import com.unicorn.server.domain.schedule.port.dto.UpdateScheduleCommand
import org.springframework.stereotype.Service

@Service
class ScheduleService :
	CreateScheduleInPort,
	UpdateScheduleInPort,
	DeleteScheduleInPort,
	GetScheduleListInPort,
	GetScheduleDetailInPort,
	RegisterConfirmationInPort {

	override fun create(command: CreateScheduleCommand): Long =
		throw UnsupportedOperationException("Not yet implemented")

	override fun update(command: UpdateScheduleCommand) =
		throw UnsupportedOperationException("Not yet implemented")

	override fun delete(scheduleId: Long, circleId: Long, memberId: String) =
		throw UnsupportedOperationException("Not yet implemented")

	override fun getList(circleId: Long, memberId: String, cursor: String?, size: Int): ScheduleListResult =
		throw UnsupportedOperationException("Not yet implemented")

	override fun getDetail(scheduleId: Long, circleId: Long, memberId: String): ScheduleDetailResult =
		throw UnsupportedOperationException("Not yet implemented")

	override fun register(command: RegisterConfirmationCommand): ConfirmationType =
		throw UnsupportedOperationException("Not yet implemented")
}
