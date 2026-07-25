package com.unicorn.server.domain.notification.service

import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.NotificationInboxItem
import com.unicorn.server.domain.notification.NotificationRoute
import com.unicorn.server.domain.notification.NotificationTemplate
import com.unicorn.server.domain.notification.enums.NotificationChannel
import com.unicorn.server.domain.notification.port.`in`.NotificationRequestInPort
import com.unicorn.server.domain.notification.port.`in`.NotificationPushTokenInPort
import com.unicorn.server.domain.notification.port.`in`.NotificationSettingInPort
import com.unicorn.server.domain.notification.port.dto.RequestNotificationCommand
import com.unicorn.server.domain.notification.port.out.NotificationInboxOutPort
import com.unicorn.server.domain.notification.port.out.NotificationOutPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NotificationRequestService(
	private val notificationOutPort: NotificationOutPort,
	private val notificationInboxOutPort: NotificationInboxOutPort,
	private val notificationPushTokenInPort: NotificationPushTokenInPort,
	private val notificationSettingInPort: NotificationSettingInPort,
) : NotificationRequestInPort {

	@Transactional
	override fun request(command: RequestNotificationCommand) {
		require(command.receiverMemberId.isNotBlank()) { "Receiver member id cannot be blank" }
		require(command.eventId.isNotBlank()) { "Event id cannot be blank" }

		val type = command.payload.type
		val template = NotificationTemplate.forType(type)
		val renderedPayload = template.renderPayload(command.payload)
		val receiverDedupKey = "${type.name.lowercase()}:${command.eventId}:${command.receiverMemberId}"

		if (type.createsInbox && notificationInboxOutPort.findByDedupKey(receiverDedupKey) == null) {
			notificationInboxOutPort.save(
				NotificationInboxItem.create(
					receiverMemberId = command.receiverMemberId,
					type = type,
					title = renderedPayload.getValue(KEY_TITLE),
					body = renderedPayload.getValue(KEY_BODY),
					route = NotificationRoute.create(
						type = type.defaultRouteType,
						circleId = command.circleId,
						scheduleId = command.scheduleId,
					),
					eventId = command.eventId,
					dedupKey = receiverDedupKey,
				),
			)
		}

		if (!type.sendsPush || !isPushEnabled(command)) {
			return
		}

		notificationPushTokenInPort.getActiveReceivable(command.receiverMemberId)
			.forEach { pushToken ->
				val pushDedupKey = "$receiverDedupKey:token:${requireNotNull(pushToken.id).value}"
				if (notificationOutPort.findByDedupKey(pushDedupKey) == null) {
					notificationOutPort.save(
						Notification.create(
							channel = NotificationChannel.PUSH,
							receiver = pushToken.token,
							type = type,
							payload = renderedPayload,
							dedupKey = pushDedupKey,
						),
					)
				}
			}
	}

	private fun isPushEnabled(command: RequestNotificationCommand): Boolean {
		val settingType = command.payload.type.settingType ?: return true
		return notificationSettingInPort.getSetting(command.receiverMemberId).isEnabled(settingType)
	}

	companion object {
		private const val KEY_TITLE = "title"
		private const val KEY_BODY = "body"
	}
}
