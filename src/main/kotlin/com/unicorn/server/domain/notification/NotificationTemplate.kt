package com.unicorn.server.domain.notification

import com.unicorn.server.domain.notification.enums.NotificationType
import com.unicorn.server.domain.notification.event.NotificationEventPayload

class NotificationTemplate(
	val type: NotificationType,
	val titleTemplate: String,
	val bodyTemplate: String,
) {
	init {
		require(titleTemplate.isNotBlank()) { "Title template cannot be blank" }
		require(bodyTemplate.isNotBlank()) { "Body template cannot be blank" }
	}

	fun renderPayload(payload: NotificationEventPayload): Map<String, String> {
		require(payload.type == type) {
			"Notification template type does not match payload: template=$type, payload=${payload.type}"
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

		fun forType(type: NotificationType): NotificationTemplate = when (type) {
			NotificationType.CIRCLE_JOIN_COMPLETED ->
				NotificationTemplate(type, "새 가족이 참여했어요", "{member_name}님이 '{circle_name}'에 참여했어요.")
			NotificationType.SCHEDULE_CREATED ->
				NotificationTemplate(type, "새 일정이 등록됐어요", "{actor_name}님이 '{schedule_title}' 일정을 추가했어요.")
			NotificationType.SCHEDULE_DELETED ->
				NotificationTemplate(type, "일정이 삭제됐어요", "{actor_name}님이 '{schedule_title}' 일정을 삭제했어요.")
			NotificationType.SCHEDULE_REMINDER_D7 ->
				NotificationTemplate(type, "일주일 후 일정이 있어요", "'{schedule_title}'까지 일주일 남았어요. 미리 준비해보는 건 어떨까요?")
			NotificationType.SCHEDULE_REMINDER_D1 ->
				NotificationTemplate(type, "내일, 잊지 말아주세요", "내일은 '{schedule_title}'이 있는 날이에요. 가족과 함께 준비해봐요.")
			NotificationType.SCHEDULE_REMINDER_DDAY_ALL_DAY ->
				NotificationTemplate(type, "오늘 일정이 있어요", "오늘은 '{schedule_title}'이 있는 날이에요. 가족과 함께 마음을 나눠보아요.")
			NotificationType.SCHEDULE_REMINDER_DDAY_TIMED ->
				NotificationTemplate(type, "1시간 후 일정이 있어요", "'{schedule_title}' 일정이 {start_time}에 시작돼요. 미리 확인해 주세요.")
			NotificationType.SCHEDULE_CONFIRMED_BY_FAMILY ->
				NotificationTemplate(type, "가족이 일정을 확인했어요", "{member_name}님이 '{schedule_title}' 일정을 확인했어요. 함께 챙길 준비가 되었어요.")
			NotificationType.SCHEDULE_CONFIRMATION_REQUESTED ->
				NotificationTemplate(type, "가족이 일정 확인을 기다리고 있어요", "'{schedule_title}', 아직 확인 전이에요. 확인 응답을 눌러주세요.")
			NotificationType.SCHEDULE_FAMILY_NOTIFICATION_REQUESTED ->
				NotificationTemplate(type, "{sender_name}님이 알림을 보냈어요", "{schedule_title} · {d_day}, 확인하고 같이 챙겨봐요!")
		}
	}
}
