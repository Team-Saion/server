package com.unicorn.server.infrastructure.adapter.out.notification.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.unicorn.server.infrastructure.config.NotificationProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream

@Configuration
@ConditionalOnProperty(prefix = "app.notification.fcm", name = ["enabled"], havingValue = "true")
class FcmConfig(
	private val notificationProperties: NotificationProperties,
) {
	@Bean
	fun firebaseApp(): FirebaseApp {
		FirebaseApp.getApps().firstOrNull()?.let { return it }
		val credentialsPath = notificationProperties.fcm.credentialsPath
		val projectId = notificationProperties.fcm.projectId

		FileInputStream(credentialsPath).use { inputStream ->
			val credentials = GoogleCredentials.fromStream(inputStream)
			val optionsBuilder = FirebaseOptions.builder().setCredentials(credentials)

			if (projectId.isNotBlank()) {
				optionsBuilder.setProjectId(projectId)
			}

			return FirebaseApp.initializeApp(optionsBuilder.build())
		}
	}

	@Bean
	fun firebaseMessaging(firebaseApp: FirebaseApp): FirebaseMessaging = FirebaseMessaging.getInstance(firebaseApp)
}
