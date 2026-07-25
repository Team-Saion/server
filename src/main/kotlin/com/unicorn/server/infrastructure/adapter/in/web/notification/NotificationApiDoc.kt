package com.unicorn.server.infrastructure.adapter.`in`.web.notification

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.notification.exception.NotificationErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto.NotificationInboxItemResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.notification.dto.NotificationInboxPageResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Tag(
	name = "Notification API",
	description = """
		알림 보관함 조회 및 읽음 처리 API

		## FCM 푸시 payload 규격

		FCM SDK의 `notification`에는 `title`, `body`가 전달되고, `data`에는 아래 JSON이 전달됩니다.
		`data`의 key와 value는 모두 문자열이며, 클라이언트는 `eventType`으로 알림 유형을 구분합니다.

		### 써클 참여 완료
		```json
		{
		  "member_name": "민수",
		  "circle_name": "우리 가족",
		  "eventType": "CIRCLE_JOIN_COMPLETED"
		}
		```

		### 일정 생성
		```json
		{
		  "actor_name": "민수",
		  "schedule_title": "병원 방문",
		  "schedule_id": "SC202408010000000001",
		  "eventType": "SCHEDULE_CREATED"
		}
		```

		### 일정 삭제

		현재 `SCHEDULE_DELETED`는 알림 보관함에만 저장되며 FCM 푸시는 발송하지 않습니다.
		동일한 메시지 조합 규격을 사용할 경우의 `data` 형식은 다음과 같습니다.

		```json
		{
		  "actor_name": "민수",
		  "schedule_title": "병원 방문",
		  "schedule_id": "SC202408010000000001",
		  "eventType": "SCHEDULE_DELETED"
		}
		```

		### 일정 7일 전 알림
		```json
		{
		  "schedule_title": "병원 방문",
		  "schedule_id": "SC202408010000000001",
		  "eventType": "SCHEDULE_REMINDER_D7"
		}
		```

		### 일정 1일 전 알림
		```json
		{
		  "schedule_title": "병원 방문",
		  "schedule_id": "SC202408010000000001",
		  "eventType": "SCHEDULE_REMINDER_D1"
		}
		```

		### 당일 종일 일정 알림
		```json
		{
		  "schedule_title": "병원 방문",
		  "schedule_id": "SC202408010000000001",
		  "eventType": "SCHEDULE_REMINDER_DDAY_ALL_DAY"
		}
		```

		### 당일 시간 지정 일정 알림
		```json
		{
		  "schedule_title": "병원 방문",
		  "start_time": "14:00",
		  "schedule_id": "SC202408010000000001",
		  "eventType": "SCHEDULE_REMINDER_DDAY_TIMED"
		}
		```

		### 가족의 일정 확인 완료
		```json
		{
		  "member_name": "영희",
		  "schedule_title": "병원 방문",
		  "schedule_id": "SC202408010000000001",
		  "eventType": "SCHEDULE_CONFIRMED_BY_FAMILY"
		}
		```

		### 일정 확인 요청
		```json
		{
		  "schedule_title": "병원 방문",
		  "schedule_id": "SC202408010000000001",
		  "eventType": "SCHEDULE_CONFIRMATION_REQUESTED"
		}
		```

		### 가족에게 전하기
		```json
		{
		  "sender_name": "민수",
		  "schedule_title": "병원 방문",
		  "d_day": "D-3",
		  "schedule_id": "SC202408010000000001",
		  "eventType": "SCHEDULE_FAMILY_NOTIFICATION_REQUESTED"
		}
		```
	""",
)
interface NotificationApiDoc {

	@Operation(
		summary = "알림 보관함 조회",
		description = """
			현재 인증된 멤버의 알림 보관함을 최신순으로 조회합니다.

			- Authorization 헤더의 Bearer access token이 필요합니다.
			- 로그인 사용자 본인의 알림만 반환합니다.
			- cursor는 이전 응답의 nextCursor를 전달합니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "INVALID_INPUT"),
	)
	@ApiSuccessCodeExample(NotificationInboxPageResponse::class)
	fun getInbox(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@RequestParam(required = false) cursor: Long?,
		@RequestParam(defaultValue = "20") size: Int,
	): ApiResponse<NotificationInboxPageResponse>

	@Operation(
		summary = "알림 읽음 처리",
		description = """
			현재 인증된 멤버의 알림 보관함 항목을 읽음 처리합니다.

			- 이미 읽은 알림은 멱등하게 처리합니다.
			- MVP 화면에서는 읽음/안읽음 시각적 구분을 노출하지 않습니다.
		""",
	)
	@ApiErrorCodeExamples(
		ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
		ApiErrorCodeExample(codeType = NotificationErrorCode::class, code = "NOTIFICATION_NOT_FOUND"),
	)
	@ApiSuccessCodeExample(NotificationInboxItemResponse::class)
	fun markRead(
		@Parameter(hidden = true)
		@AuthenticationPrincipal memberId: String,
		@PathVariable notificationId: Long,
	): ApiResponse<NotificationInboxItemResponse>
}
