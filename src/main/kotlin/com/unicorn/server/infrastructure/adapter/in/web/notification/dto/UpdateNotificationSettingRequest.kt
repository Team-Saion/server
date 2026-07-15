package com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto

import com.unicorn.server.domain.notification.port.dto.UpdateNotificationSettingCommand
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "알림 설정 변경 요청")
data class UpdateNotificationSettingRequest(
	@field:Schema(description = "D-7 알림 수신 여부", example = "true")
	val d7Enabled: Boolean,

	@field:Schema(description = "D-1 알림 수신 여부", example = "true")
	val d1Enabled: Boolean,

	@field:Schema(description = "D-day 알림 수신 여부", example = "true")
	val dDayEnabled: Boolean,

	@field:Schema(description = "가족 일정 확인 알림 수신 여부", example = "true")
	val familyScheduleCheckEnabled: Boolean,
) {
	fun toCommand(): UpdateNotificationSettingCommand = UpdateNotificationSettingCommand(
		d7Enabled = d7Enabled,
		d1Enabled = d1Enabled,
		dDayEnabled = dDayEnabled,
		familyScheduleCheckEnabled = familyScheduleCheckEnabled,
	)
}
