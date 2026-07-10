package com.unicorn.server.domain.notification

import com.unicorn.server.domain.notification.enums.DevicePlatform
import com.unicorn.server.domain.notification.vo.DevicePushTokenId
import java.time.LocalDateTime

// DevicePushToken 도메인 - 멤버 디바이스의 푸시 수신 가능 상태와 최신 접속 정보
class DevicePushToken private constructor(
	// 디바이스 푸시 토큰 고유 식별자 (신규 생성 시 저장 전까지 null 가능)
	val id: DevicePushTokenId?,
	// 푸시 토큰 소유 멤버 식별자
	val memberId: String,
	// 푸시 발송용 실제 디바이스 토큰 문자열
	token: String,
	// 토큰 등록 디바이스 플랫폼
	platform: DevicePlatform,
	// 운영체제 수준 푸시 권한 허용 여부
	osNotificationPermissionGranted: Boolean,
	// 토큰 등록 앱 버전
	appVersion: String?,
	// 현재 토큰 활성 상태 여부
	active: Boolean,
	// 마지막 토큰 확인 시각
	lastSeenAt: LocalDateTime,
	// 토큰 비활성화 시각 (유효하면 null)
	invalidatedAt: LocalDateTime?,
	// 토큰 최초 등록 시각
	val createdAt: LocalDateTime,
	// 토큰 상태 최종 변경 시각
	updatedAt: LocalDateTime,
) {
	var token: String = token
		private set

	var platform: DevicePlatform = platform
		private set

	var osNotificationPermissionGranted: Boolean = osNotificationPermissionGranted
		private set

	var appVersion: String? = appVersion
		private set

	var active: Boolean = active
		private set

	var lastSeenAt: LocalDateTime = lastSeenAt
		private set

	var invalidatedAt: LocalDateTime? = invalidatedAt
		private set

	var updatedAt: LocalDateTime = updatedAt
		private set

	fun refresh(
		platform: DevicePlatform,
		osNotificationPermissionGranted: Boolean,
		appVersion: String?,
		now: LocalDateTime = LocalDateTime.now(),
	) {
		this.platform = platform
		this.osNotificationPermissionGranted = osNotificationPermissionGranted
		this.appVersion = appVersion
		active = true
		lastSeenAt = now
		invalidatedAt = null
		updatedAt = now
	}

	fun deactivate(now: LocalDateTime = LocalDateTime.now()) {
		if (!active) {
			return
		}

		active = false
		invalidatedAt = now
		updatedAt = now
	}

	fun canReceivePush(): Boolean = active && osNotificationPermissionGranted

	companion object {
		fun register(
			memberId: String,
			token: String,
			platform: DevicePlatform,
			osNotificationPermissionGranted: Boolean,
			appVersion: String?,
		): DevicePushToken {
			validate(memberId, token)
			val now = LocalDateTime.now()
			return DevicePushToken(
				id = null,
				memberId = memberId,
				token = token,
				platform = platform,
				osNotificationPermissionGranted = osNotificationPermissionGranted,
				appVersion = appVersion,
				active = true,
				lastSeenAt = now,
				invalidatedAt = null,
				createdAt = now,
				updatedAt = now,
			)
		}

		fun reconstitute(
			id: DevicePushTokenId,
			memberId: String,
			token: String,
			platform: DevicePlatform,
			osNotificationPermissionGranted: Boolean,
			appVersion: String?,
			active: Boolean,
			lastSeenAt: LocalDateTime,
			invalidatedAt: LocalDateTime?,
			createdAt: LocalDateTime,
			updatedAt: LocalDateTime,
		): DevicePushToken {
			validate(memberId, token)
			return DevicePushToken(
				id = id,
				memberId = memberId,
				token = token,
				platform = platform,
				osNotificationPermissionGranted = osNotificationPermissionGranted,
				appVersion = appVersion,
				active = active,
				lastSeenAt = lastSeenAt,
				invalidatedAt = invalidatedAt,
				createdAt = createdAt,
				updatedAt = updatedAt,
			)
		}

		private fun validate(memberId: String, token: String) {
			require(memberId.isNotBlank()) { "Member id cannot be blank" }
			require(token.isNotBlank()) { "Push token cannot be blank" }
		}
	}
}
