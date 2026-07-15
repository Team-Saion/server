package com.unicorn.server.domain.notification

import com.unicorn.server.domain.notification.enums.NotificationRouteType

// NotificationRoute 도메인 - 알림 클릭 시 앱 내부 이동 목적지 정보
class NotificationRoute private constructor(
	// 이동 목적지 라우트 유형
	val type: NotificationRouteType,
	// 서클 홈 또는 서클 일정 목록 이동용 서클 식별자
	val circleId: String?,
	// 일정 상세 이동용 일정 식별자
	val scheduleId: String?,
) {
	companion object {
		fun create(
			type: NotificationRouteType,
			circleId: String? = null,
			scheduleId: String? = null,
		): NotificationRoute {
			when (type) {
				NotificationRouteType.CIRCLE_HOME -> require(!circleId.isNullOrBlank()) { "Circle route requires circleId" }
				NotificationRouteType.SCHEDULE_DETAIL -> require(!scheduleId.isNullOrBlank()) { "Schedule detail route requires scheduleId" }
				NotificationRouteType.SCHEDULE_LIST -> require(!circleId.isNullOrBlank()) { "Schedule list route requires circleId" }
				NotificationRouteType.HOME -> Unit
			}

			return NotificationRoute(type, circleId, scheduleId)
		}
	}
}
