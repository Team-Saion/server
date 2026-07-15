package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.NotificationSetting
import com.unicorn.server.domain.notification.port.`in`.NotificationSettingInPort
import com.unicorn.server.domain.notification.port.dto.UpdateNotificationSettingCommand
import com.unicorn.server.domain.notification.port.out.NotificationSettingOutPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NotificationSettingService(
	private val notificationSettingOutPort: NotificationSettingOutPort,
) : NotificationSettingInPort {

	@Transactional
	override fun getSetting(memberId: String): NotificationSetting {
		require(memberId.isNotBlank()) { "Member id cannot be blank" }
		return notificationSettingOutPort.findByMemberId(memberId)
			?: notificationSettingOutPort.save(NotificationSetting.default(memberId))
	}

	@Transactional
	override fun updateSetting(memberId: String, command: UpdateNotificationSettingCommand): NotificationSetting {
		require(memberId.isNotBlank()) { "Member id cannot be blank" }
		val setting = notificationSettingOutPort.findByMemberId(memberId)
			?: NotificationSetting.default(memberId)

		setting.update(
			d7Enabled = command.d7Enabled,
			d1Enabled = command.d1Enabled,
			dDayEnabled = command.dDayEnabled,
			familyScheduleCheckEnabled = command.familyScheduleCheckEnabled,
		)

		return notificationSettingOutPort.save(setting)
	}
}
