package com.unicorn.server.domain.notification.port.out

import com.unicorn.server.domain.notification.NotificationTemplate
import com.unicorn.server.domain.notification.enums.NotificationEventType

interface NotificationTemplateOutPort {
	fun findActiveByEventType(eventType: NotificationEventType): NotificationTemplate?
}
