package com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto

import com.unicorn.server.domain.notification.NotificationInboxItem
import com.unicorn.server.domain.notification.enums.NotificationType
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "알림 보관함 항목 응답")
data class NotificationInboxItemResponse(
	@field:Schema(description = "알림 식별자", example = "1")
	val id: Long,

	@field:Schema(description = "알림 유형", example = "SCHEDULE_CREATED")
	val type: NotificationType,

	@field:Schema(description = "알림 제목", example = "새 일정이 등록됐어요")
	val title: String,

	@field:Schema(description = "알림 본문", example = "민수님이 '병원 방문' 일정을 추가했어요.")
	val body: String,

	@field:Schema(description = "알림 발생 시각")
	val occurredAt: LocalDateTime,

	@field:Schema(description = "읽음 처리 시각. MVP 화면에서는 읽음 여부를 별도 노출하지 않는다.", nullable = true)
	val readAt: LocalDateTime?,

	@field:Schema(description = "클릭 시 이동 정보")
	val route: NotificationRouteResponse,
) {
	companion object {
		fun from(item: NotificationInboxItem): NotificationInboxItemResponse = NotificationInboxItemResponse(
			id = requireNotNull(item.id) { "Notification inbox item id must not be null" }.value,
			type = item.type,
			title = item.title,
			body = item.body,
			occurredAt = item.createdAt,
			readAt = item.readAt,
			route = NotificationRouteResponse.from(item.route),
		)
	}
}
