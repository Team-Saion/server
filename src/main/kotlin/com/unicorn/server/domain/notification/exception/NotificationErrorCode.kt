package com.unicorn.server.domain.notification.exception

import com.unicorn.server.common.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class NotificationErrorCode(
	override val code: String,
	override val message: String,
	override val httpStatus: HttpStatus,
) : ErrorCode {
	NOTIFICATION_NOT_FOUND("N404_1", "Notification not found", HttpStatus.NOT_FOUND),
}
