package com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.unicorn.server.domain.notification.DevicePushToken
import com.unicorn.server.domain.notification.Notification
import com.unicorn.server.domain.notification.NotificationInboxItem
import com.unicorn.server.domain.notification.NotificationRoute
import com.unicorn.server.domain.notification.NotificationSetting
import com.unicorn.server.domain.notification.vo.DevicePushTokenId
import com.unicorn.server.domain.notification.vo.NotificationId
import com.unicorn.server.domain.notification.vo.NotificationInboxItemId

fun Notification.toEntity(objectMapper: ObjectMapper): NotificationEntity = NotificationEntity().apply {
	id = this@toEntity.id?.value
	channel = this@toEntity.channel
	receiver = this@toEntity.receiver
	eventType = this@toEntity.eventType
	payload = objectMapper.writeValueAsString(this@toEntity.payload)
	status = this@toEntity.status
	attemptCount = this@toEntity.attemptCount
	nextRetryAt = this@toEntity.nextRetryAt
	sentAt = this@toEntity.sentAt
	failedAt = this@toEntity.failedAt
	failReason = this@toEntity.failReason
	dedupKey = this@toEntity.dedupKey
	createdAt = this@toEntity.createdAt
	updatedAt = this@toEntity.updatedAt
}

fun NotificationEntity.toDomain(
	objectMapper: ObjectMapper,
	payloadTypeReference: TypeReference<Map<String, String>>,
): Notification = Notification.reconstitute(
	id = NotificationId.of(requireNotNull(id) { "id must not be null" }),
	channel = channel,
	receiver = receiver,
	eventType = eventType,
	payload = objectMapper.readValue(payload, payloadTypeReference),
	dedupKey = dedupKey,
	status = status,
	attemptCount = attemptCount,
	nextRetryAt = nextRetryAt,
	sentAt = sentAt,
	failedAt = failedAt,
	failReason = failReason,
	createdAt = requireNotNull(createdAt) { "createdAt must not be null" },
	updatedAt = requireNotNull(updatedAt) { "updatedAt must not be null" },
)

fun NotificationInboxItem.toEntity(): NotificationInboxItemEntity = NotificationInboxItemEntity().apply {
	id = this@toEntity.id?.value
	receiverMemberId = this@toEntity.receiverMemberId
	type = this@toEntity.type
	title = this@toEntity.title
	body = this@toEntity.body
	routeType = this@toEntity.route.type
	circleId = this@toEntity.route.circleId
	scheduleId = this@toEntity.route.scheduleId
	eventId = this@toEntity.eventId
	dedupKey = this@toEntity.dedupKey
	readAt = this@toEntity.readAt
	createdAt = this@toEntity.createdAt
	updatedAt = this@toEntity.updatedAt
}

fun NotificationInboxItemEntity.toDomain(): NotificationInboxItem = NotificationInboxItem.reconstitute(
	id = NotificationInboxItemId.of(requireNotNull(id) { "id must not be null" }),
	receiverMemberId = receiverMemberId,
	type = type,
	title = title,
	body = body,
	route = NotificationRoute.create(routeType, circleId, scheduleId),
	eventId = eventId,
	dedupKey = dedupKey,
	readAt = readAt,
	createdAt = requireNotNull(createdAt) { "createdAt must not be null" },
	updatedAt = requireNotNull(updatedAt) { "updatedAt must not be null" },
)

fun NotificationSetting.toEntity(): NotificationSettingEntity = NotificationSettingEntity().apply {
	memberId = this@toEntity.memberId
	d7Enabled = this@toEntity.d7Enabled
	d1Enabled = this@toEntity.d1Enabled
	dDayEnabled = this@toEntity.dDayEnabled
	familyScheduleCheckEnabled = this@toEntity.familyScheduleCheckEnabled
	createdAt = this@toEntity.createdAt
	updatedAt = this@toEntity.updatedAt
}

fun NotificationSettingEntity.toDomain(): NotificationSetting = NotificationSetting.reconstitute(
	memberId = memberId,
	d7Enabled = d7Enabled,
	d1Enabled = d1Enabled,
	dDayEnabled = dDayEnabled,
	familyScheduleCheckEnabled = familyScheduleCheckEnabled,
	createdAt = requireNotNull(createdAt) { "createdAt must not be null" },
	updatedAt = requireNotNull(updatedAt) { "updatedAt must not be null" },
)

fun DevicePushToken.toEntity(): DevicePushTokenEntity = DevicePushTokenEntity().apply {
	id = this@toEntity.id?.value
	memberId = this@toEntity.memberId
	token = this@toEntity.token
	platform = this@toEntity.platform
	osNotificationPermissionGranted = this@toEntity.osNotificationPermissionGranted
	appVersion = this@toEntity.appVersion
	active = this@toEntity.active
	lastSeenAt = this@toEntity.lastSeenAt
	invalidatedAt = this@toEntity.invalidatedAt
	createdAt = this@toEntity.createdAt
	updatedAt = this@toEntity.updatedAt
}

fun DevicePushTokenEntity.toDomain(): DevicePushToken = DevicePushToken.reconstitute(
	id = DevicePushTokenId.of(requireNotNull(id) { "id must not be null" }),
	memberId = memberId,
	token = token,
	platform = platform,
	osNotificationPermissionGranted = osNotificationPermissionGranted,
	appVersion = appVersion,
	active = active,
	lastSeenAt = lastSeenAt,
	invalidatedAt = invalidatedAt,
	createdAt = requireNotNull(createdAt) { "createdAt must not be null" },
	updatedAt = requireNotNull(updatedAt) { "updatedAt must not be null" },
)
