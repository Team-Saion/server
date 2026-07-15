package com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto

import com.unicorn.server.domain.notification.port.dto.NotificationInboxPage
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "알림 보관함 페이지 응답")
data class NotificationInboxPageResponse(
	@field:Schema(description = "알림 목록")
	val items: List<NotificationInboxItemResponse>,

	@field:Schema(description = "다음 페이지 커서", nullable = true, example = "10")
	val nextCursor: Long?,
) {
	companion object {
		fun from(page: NotificationInboxPage): NotificationInboxPageResponse = NotificationInboxPageResponse(
			items = page.items.map(NotificationInboxItemResponse::from),
			nextCursor = page.nextCursor,
		)
	}
}
