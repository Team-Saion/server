package com.unicorn.server.domain.notification.port.`in`

import com.unicorn.server.domain.notification.NotificationSetting
import com.unicorn.server.domain.notification.port.dto.UpdateNotificationSettingCommand

interface NotificationSettingInPort {
	fun getSetting(memberId: String): NotificationSetting
	fun updateSetting(memberId: String, command: UpdateNotificationSettingCommand): NotificationSetting
}
