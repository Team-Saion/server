package com.unicorn.server.domain.notification.port.`in`

interface NotificationInboxMaintenanceInPort {
	fun deleteExpired(retentionDays: Long = DEFAULT_RETENTION_DAYS): Int

	companion object {
		const val DEFAULT_RETENTION_DAYS: Long = 90
	}
}
