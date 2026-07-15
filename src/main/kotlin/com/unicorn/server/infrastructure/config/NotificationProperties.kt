package com.unicorn.server.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.notification")
class NotificationProperties {
	var dispatch: Dispatch = Dispatch()
	var fcm: Fcm = Fcm()

	class Dispatch {
		var enabled: Boolean = false
		var intervalMs: Long = 30000
		var batchSize: Int = 100
		var maxAttempts: Int = 3
		var baseRetryDelayMinutes: Long = 5
	}

	class Fcm {
		var enabled: Boolean = false
		var projectId: String = ""
		var credentialsPath: String = ""
	}
}
