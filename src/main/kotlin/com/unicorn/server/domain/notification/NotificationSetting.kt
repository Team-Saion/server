package com.unicorn.server.domain.notification

import com.unicorn.server.domain.notification.enums.NotificationSettingType
import java.time.LocalDateTime

// NotificationSetting 도메인 - 멤버별 알림 수신 여부 설정
class NotificationSetting private constructor(
	// 알림 설정 소유 멤버 식별자
	val memberId: String,
	// 일정 7일 전 알림 수신 여부
	d7Enabled: Boolean,
	// 일정 1일 전 알림 수신 여부
	d1Enabled: Boolean,
	// 일정 당일 알림 수신 여부
	dDayEnabled: Boolean,
	// 가족 일정 확인 알림 수신 여부
	familyScheduleCheckEnabled: Boolean,
	// 설정 최초 생성 시각
	val createdAt: LocalDateTime,
	// 설정 최종 변경 시각
	updatedAt: LocalDateTime,
) {
	var d7Enabled: Boolean = d7Enabled
		private set

	var d1Enabled: Boolean = d1Enabled
		private set

	var dDayEnabled: Boolean = dDayEnabled
		private set

	var familyScheduleCheckEnabled: Boolean = familyScheduleCheckEnabled
		private set

	var updatedAt: LocalDateTime = updatedAt
		private set

	fun update(
		d7Enabled: Boolean,
		d1Enabled: Boolean,
		dDayEnabled: Boolean,
		familyScheduleCheckEnabled: Boolean,
		now: LocalDateTime = LocalDateTime.now(),
	) {
		this.d7Enabled = d7Enabled
		this.d1Enabled = d1Enabled
		this.dDayEnabled = dDayEnabled
		this.familyScheduleCheckEnabled = familyScheduleCheckEnabled
		updatedAt = now
	}

	fun isEnabled(type: NotificationSettingType): Boolean = when (type) {
		NotificationSettingType.D7 -> d7Enabled
		NotificationSettingType.D1 -> d1Enabled
		NotificationSettingType.D_DAY -> dDayEnabled
		NotificationSettingType.FAMILY_SCHEDULE_CHECK -> familyScheduleCheckEnabled
	}

	companion object {
		fun default(memberId: String): NotificationSetting {
			validateMemberId(memberId)
			val now = LocalDateTime.now()
			return NotificationSetting(
				memberId = memberId,
				d7Enabled = true,
				d1Enabled = true,
				dDayEnabled = true,
				familyScheduleCheckEnabled = true,
				createdAt = now,
				updatedAt = now,
			)
		}

		fun reconstitute(
			memberId: String,
			d7Enabled: Boolean,
			d1Enabled: Boolean,
			dDayEnabled: Boolean,
			familyScheduleCheckEnabled: Boolean,
			createdAt: LocalDateTime,
			updatedAt: LocalDateTime,
		): NotificationSetting {
			validateMemberId(memberId)
			return NotificationSetting(
				memberId = memberId,
				d7Enabled = d7Enabled,
				d1Enabled = d1Enabled,
				dDayEnabled = dDayEnabled,
				familyScheduleCheckEnabled = familyScheduleCheckEnabled,
				createdAt = createdAt,
				updatedAt = updatedAt,
			)
		}

		private fun validateMemberId(memberId: String) {
			require(memberId.isNotBlank()) { "Member id cannot be blank" }
		}
	}
}
