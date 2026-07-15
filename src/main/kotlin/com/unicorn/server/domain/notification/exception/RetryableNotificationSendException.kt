package com.unicorn.server.domain.notification.exception

class RetryableNotificationSendException(
	message: String,
	cause: Throwable? = null,
) : RuntimeException(message, cause)
