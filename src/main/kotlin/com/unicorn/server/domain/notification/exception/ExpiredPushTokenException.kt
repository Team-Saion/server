package com.unicorn.server.domain.notification.exception

class ExpiredPushTokenException(
	message: String,
	cause: Throwable? = null,
) : RuntimeException(message, cause)
