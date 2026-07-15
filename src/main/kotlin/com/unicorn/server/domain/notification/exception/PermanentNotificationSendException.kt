package com.unicorn.server.domain.notification.exception

class PermanentNotificationSendException(
	message: String,
	cause: Throwable? = null,
) : RuntimeException(message, cause)
