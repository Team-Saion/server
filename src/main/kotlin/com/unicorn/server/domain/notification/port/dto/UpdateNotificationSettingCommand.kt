package com.unicorn.server.domain.notification.port.dto

data class UpdateNotificationSettingCommand(
	val d7Enabled: Boolean,
	val d1Enabled: Boolean,
	val dDayEnabled: Boolean,
	val familyScheduleCheckEnabled: Boolean,
)
