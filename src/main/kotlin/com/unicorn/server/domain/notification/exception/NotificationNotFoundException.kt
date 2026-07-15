package com.unicorn.server.domain.notification.exception

import com.unicorn.server.common.exception.BusinessException

class NotificationNotFoundException(notificationId: Long) :
	BusinessException(NotificationErrorCode.NOTIFICATION_NOT_FOUND, "notificationId=$notificationId")
