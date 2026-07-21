package com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.unicorn.server.domain.notification.NotificationSetting
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "알림 설정 응답")
data class NotificationSettingResponse(
	@field:Schema(description = "D-7 알림 수신 여부", example = "true")
	val d7Enabled: Boolean,

	@field:Schema(description = "D-1 알림 수신 여부", example = "true")
	val d1Enabled: Boolean,

	@get:JsonProperty("dDayEnabled")
	@get:Schema(name = "dDayEnabled", description = "D-day 알림 수신 여부", example = "true")
	val dDayEnabled: Boolean,

	@field:Schema(description = "가족 일정 확인 알림 수신 여부", example = "true")
	val familyScheduleCheckEnabled: Boolean,
) {
	companion object {
		fun from(setting: NotificationSetting): NotificationSettingResponse = NotificationSettingResponse(
			d7Enabled = setting.d7Enabled,
			d1Enabled = setting.d1Enabled,
			dDayEnabled = setting.dDayEnabled,
			familyScheduleCheckEnabled = setting.familyScheduleCheckEnabled,
		)
	}
}
