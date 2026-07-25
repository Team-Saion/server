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
import org.slf4j.LoggerFactory
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
		log.debug(
			"[NotificationRequest] notification request started: type={}, eventId={}, receiverMemberId={}, createsInbox={}, sendsPush={}, dedupKey={}",
			type,
			command.eventId,
			command.receiverMemberId,
			type.createsInbox,
			type.sendsPush,
			receiverDedupKey,
		)

		val existingInboxItem = notificationInboxOutPort.findByDedupKey(receiverDedupKey)
		log.debug(
			"[NotificationRequest] inbox dedup checked: dedupKey={}, exists={}",
			receiverDedupKey,
			existingInboxItem != null,
		)
		if (type.createsInbox && existingInboxItem == null) {
			val savedInboxItem = notificationInboxOutPort.save(
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
			log.debug(
				"[NotificationRequest] inbox item saved: inboxItemId={}, dedupKey={}, receiverMemberId={}",
				savedInboxItem.id?.value,
				receiverDedupKey,
				command.receiverMemberId,
			)
		} else {
			log.debug(
				"[NotificationRequest] inbox save skipped: createsInbox={}, alreadyExists={}, dedupKey={}",
				type.createsInbox,
				existingInboxItem != null,
				receiverDedupKey,
			)
		}

		val pushEnabled = isPushEnabled(command)
		log.debug(
			"[NotificationRequest] push policy checked: type={}, sendsPush={}, settingEnabled={}, receiverMemberId={}",
			type,
			type.sendsPush,
			pushEnabled,
			command.receiverMemberId,
		)
		if (!type.sendsPush || !pushEnabled) {
			log.debug(
				"[NotificationRequest] push queue creation skipped: type={}, receiverMemberId={}",
				type,
				command.receiverMemberId,
			)
			return
		}

		val pushTokens = notificationPushTokenInPort.getActiveReceivable(command.receiverMemberId)
		log.debug(
			"[NotificationRequest] active push tokens loaded: receiverMemberId={}, tokenCount={}, tokenIds={}",
			command.receiverMemberId,
			pushTokens.size,
			pushTokens.mapNotNull { it.id?.value },
		)
		pushTokens.forEach { pushToken ->
			val pushTokenId = requireNotNull(pushToken.id).value
			val pushDedupKey = "$receiverDedupKey:token:$pushTokenId"
			val existingNotification = notificationOutPort.findByDedupKey(pushDedupKey)
			log.debug(
				"[NotificationRequest] push dedup checked: dedupKey={}, pushTokenId={}, exists={}",
				pushDedupKey,
				pushTokenId,
				existingNotification != null,
			)
			if (existingNotification == null) {
				val savedNotification = notificationOutPort.save(
					Notification.create(
						channel = NotificationChannel.PUSH,
						receiver = pushToken.token,
						type = type,
						payload = renderedPayload,
						dedupKey = pushDedupKey,
					),
				)
				log.debug(
					"[NotificationRequest] push notification queued: notificationId={}, dedupKey={}, pushTokenId={}, status={}",
					savedNotification.id?.value,
					pushDedupKey,
					pushTokenId,
					savedNotification.status,
				)
			} else {
				log.debug(
					"[NotificationRequest] push queue save skipped because it already exists: notificationId={}, dedupKey={}",
					existingNotification.id?.value,
					pushDedupKey,
				)
			}
		}
		log.debug(
			"[NotificationRequest] notification request completed: type={}, eventId={}, receiverMemberId={}",
			type,
			command.eventId,
			command.receiverMemberId,
		)
	}

	private fun isPushEnabled(command: RequestNotificationCommand): Boolean {
		val settingType = command.payload.type.settingType ?: return true
		return notificationSettingInPort.getSetting(command.receiverMemberId).isEnabled(settingType)
	}

	companion object {
		private const val KEY_TITLE = "title"
		private const val KEY_BODY = "body"
		private val log = LoggerFactory.getLogger(NotificationRequestService::class.java)
	}
}
