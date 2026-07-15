package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.NotificationSetting
import com.unicorn.server.domain.notification.enums.NotificationSettingType
import com.unicorn.server.domain.notification.port.dto.UpdateNotificationSettingCommand
import com.unicorn.server.domain.notification.port.out.NotificationSettingOutPort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("NotificationSettingService 단위 테스트")
class NotificationSettingServiceTest {
	private val notificationSettingOutPort = FakeNotificationSettingOutPort()
	private val notificationSettingService = NotificationSettingService(notificationSettingOutPort)

	@Test
	@DisplayName("알림 설정이 없으면 기본값 ON으로 생성한다")
	fun getSetting_withoutExistingSetting_createsDefaultOnSetting() {
		val setting = notificationSettingService.getSetting("member-1")

		assertThat(setting.d7Enabled).isTrue()
		assertThat(setting.d1Enabled).isTrue()
		assertThat(setting.dDayEnabled).isTrue()
		assertThat(setting.familyScheduleCheckEnabled).isTrue()
		assertThat(notificationSettingOutPort.findByMemberId("member-1")).isNotNull()
	}

	@Test
	@DisplayName("알림 설정 변경 시 변경된 값이 저장된다")
	fun updateSetting_success_savesChangedValues() {
		val command = UpdateNotificationSettingCommand(
			d7Enabled = false,
			d1Enabled = true,
			dDayEnabled = false,
			familyScheduleCheckEnabled = true,
		)

		val setting = notificationSettingService.updateSetting("member-1", command)

		assertThat(setting.d7Enabled).isFalse()
		assertThat(setting.d1Enabled).isTrue()
		assertThat(setting.dDayEnabled).isFalse()
		assertThat(setting.familyScheduleCheckEnabled).isTrue()
	}

	@Test
	@DisplayName("설정 유형별 ON/OFF 여부를 반환한다")
	fun isEnabled_bySettingType_returnsMatchingValue() {
		val setting = notificationSettingService.updateSetting(
			"member-1",
			UpdateNotificationSettingCommand(
				d7Enabled = false,
				d1Enabled = true,
				dDayEnabled = false,
				familyScheduleCheckEnabled = true,
			),
		)

		assertThat(setting.isEnabled(NotificationSettingType.D7)).isFalse()
		assertThat(setting.isEnabled(NotificationSettingType.D1)).isTrue()
		assertThat(setting.isEnabled(NotificationSettingType.D_DAY)).isFalse()
		assertThat(setting.isEnabled(NotificationSettingType.FAMILY_SCHEDULE_CHECK)).isTrue()
	}

	private class FakeNotificationSettingOutPort : NotificationSettingOutPort {
		private val settings = linkedMapOf<String, NotificationSetting>()

		override fun save(setting: NotificationSetting): NotificationSetting {
			settings[setting.memberId] = setting
			return setting
		}

		override fun findByMemberId(memberId: String): NotificationSetting? = settings[memberId]
	}
}
