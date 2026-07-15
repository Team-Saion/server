package com.unicorn.server.domain.notification.enums

enum class NotificationType(
	val createsInbox: Boolean,
	val sendsPush: Boolean,
	val settingType: NotificationSettingType?,
	val defaultRouteType: NotificationRouteType,
) {
	CIRCLE_JOIN_COMPLETED(
		createsInbox = true,
		sendsPush = false,
		settingType = null,
		defaultRouteType = NotificationRouteType.CIRCLE_HOME,
	),
	SCHEDULE_CREATED(
		createsInbox = true,
		sendsPush = true,
		settingType = null,
		defaultRouteType = NotificationRouteType.SCHEDULE_DETAIL,
	),
	SCHEDULE_DELETED(
		createsInbox = true,
		sendsPush = false,
		settingType = null,
		defaultRouteType = NotificationRouteType.SCHEDULE_LIST,
	),
	SCHEDULE_REMINDER_D7(
		createsInbox = true,
		sendsPush = true,
		settingType = NotificationSettingType.D7,
		defaultRouteType = NotificationRouteType.SCHEDULE_DETAIL,
	),
	SCHEDULE_REMINDER_D1(
		createsInbox = true,
		sendsPush = true,
		settingType = NotificationSettingType.D1,
		defaultRouteType = NotificationRouteType.SCHEDULE_DETAIL,
	),
	SCHEDULE_REMINDER_DDAY_ALL_DAY(
		createsInbox = true,
		sendsPush = true,
		settingType = NotificationSettingType.D_DAY,
		defaultRouteType = NotificationRouteType.SCHEDULE_DETAIL,
	),
	SCHEDULE_REMINDER_DDAY_TIMED(
		createsInbox = true,
		sendsPush = true,
		settingType = NotificationSettingType.D_DAY,
		defaultRouteType = NotificationRouteType.SCHEDULE_DETAIL,
	),
	SCHEDULE_CONFIRMED_BY_FAMILY(
		createsInbox = true,
		sendsPush = true,
		settingType = NotificationSettingType.FAMILY_SCHEDULE_CHECK,
		defaultRouteType = NotificationRouteType.SCHEDULE_DETAIL,
	),
	SCHEDULE_CONFIRMATION_REQUESTED(
		createsInbox = true,
		sendsPush = true,
		settingType = NotificationSettingType.FAMILY_SCHEDULE_CHECK,
		defaultRouteType = NotificationRouteType.SCHEDULE_DETAIL,
	),
}
