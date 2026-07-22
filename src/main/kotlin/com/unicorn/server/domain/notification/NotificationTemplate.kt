package com.unicorn.server.domain.notification

import com.unicorn.server.domain.notification.enums.NotificationEventType
import com.unicorn.server.domain.notification.event.NotificationEventPayload

class NotificationTemplate(
	val eventType: NotificationEventType,
	val titleTemplate: String,
	val bodyTemplate: String,
) {
	init {
		require(titleTemplate.isNotBlank()) { "Title template cannot be blank" }
		require(bodyTemplate.isNotBlank()) { "Body template cannot be blank" }
	}

	fun renderPayload(payload: NotificationEventPayload): Map<String, String> {
		require(payload.eventType == eventType) {
			"Notification template event type does not match payload: template=$eventType, payload=${payload.eventType}"
		}
		val variables = payload.toVariables()
		val templateVariables = extractVariables(titleTemplate) + extractVariables(bodyTemplate)
		require(templateVariables == variables.keys) {
			"Notification template variables do not match payload: " +
				"expected=${variables.keys.sorted()}, actual=${templateVariables.sorted()}"
		}

		return variables + mapOf(
			KEY_TITLE to render(titleTemplate, variables),
			KEY_BODY to render(bodyTemplate, variables),
		)
	}

	private fun render(template: String, variables: Map<String, String>): String =
		VARIABLE_PATTERN.replace(template) { matchResult ->
			variables.getValue(matchResult.groupValues[1])
		}

	private fun extractVariables(template: String): Set<String> =
		VARIABLE_PATTERN.findAll(template)
			.map { it.groupValues[1] }
			.toSet()

	companion object {
		private const val KEY_TITLE = "title"
		private const val KEY_BODY = "body"
		private val VARIABLE_PATTERN = Regex("\\{([A-Za-z0-9_]+)}")
	}
}
