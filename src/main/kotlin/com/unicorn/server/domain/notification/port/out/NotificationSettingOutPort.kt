package com.unicorn.server.domain.notification.port.out

import com.unicorn.server.domain.notification.NotificationSetting

interface NotificationSettingOutPort {
	fun save(setting: NotificationSetting): NotificationSetting

	fun findByMemberId(memberId: String): NotificationSetting?
}
