package com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto

import com.unicorn.server.domain.notification.NotificationRoute
import com.unicorn.server.domain.notification.enums.NotificationRouteType
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "알림 클릭 라우팅 응답")
data class NotificationRouteResponse(
	@field:Schema(description = "이동 화면 유형", example = "SCHEDULE_DETAIL")
	val type: NotificationRouteType,

	@field:Schema(description = "써클 식별자", nullable = true, example = "circle-id")
	val circleId: String?,

	@field:Schema(description = "일정 식별자", nullable = true, example = "schedule-id")
	val scheduleId: String?,
) {
	companion object {
		fun from(route: NotificationRoute): NotificationRouteResponse = NotificationRouteResponse(
			type = route.type,
			circleId = route.circleId,
			scheduleId = route.scheduleId,
		)
	}
}
