package com.unicorn.server.domain.notification.enums

enum class NotificationEventType {
	/** 써클 참여 완료 */
	CIRCLE_JOIN_COMPLETED,

	/** 일정 생성 */
	SCHEDULE_CREATED,

	/** 일정 삭제 */
	SCHEDULE_DELETED,

	/** D-7 리마인드 */
	SCHEDULE_REMINDER_D7,

	/** D-1 리마인드 */
	SCHEDULE_REMINDER_D1,

	/** D-day 종일 일정 리마인드 */
	SCHEDULE_REMINDER_DDAY_ALL_DAY,

	/** D-day 시간 지정 일정 리마인드 */
	SCHEDULE_REMINDER_DDAY_TIMED,

	/** 가족 확인 완료 */
	SCHEDULE_CONFIRMED_BY_FAMILY,

	/** 미확인 일정 확인 요청 */
	SCHEDULE_CONFIRMATION_REQUESTED,
}
