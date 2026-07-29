package com.unicorn.server.infrastructure.adapter.`in`.event.schedule

import com.unicorn.server.domain.circle.port.`in`.CircleMemberInPort
import com.unicorn.server.domain.circle.port.dto.CircleMemberDto
import com.unicorn.server.domain.circle.port.dto.CircleSummary
import com.unicorn.server.domain.circle.port.dto.JoinCircleResult
import com.unicorn.server.domain.notification.enums.NotificationType
import com.unicorn.server.domain.notification.port.`in`.NotificationPublishInPort
import com.unicorn.server.domain.notification.port.dto.PublishNotificationCommand
import com.unicorn.server.domain.schedule.enums.ScheduleReminderType
import com.unicorn.server.domain.schedule.event.FamilyScheduleNotificationRequestedEvent
import com.unicorn.server.domain.schedule.event.ScheduleConfirmationRequestDueEvent
import com.unicorn.server.domain.schedule.event.ScheduleConfirmedEvent
import com.unicorn.server.domain.schedule.event.ScheduleCreatedEvent
import com.unicorn.server.domain.schedule.event.ScheduleDeletedEvent
import com.unicorn.server.domain.schedule.event.ScheduleReminderDueEvent
import com.unicorn.server.domain.schedule.port.`in`.ScheduleConfirmationStatusInPort
import com.unicorn.server.domain.schedule.vo.ScheduleId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("ScheduleNotificationEventListener 단위 테스트")
class ScheduleNotificationEventListenerTest {
	@Test
	@DisplayName("일정 생성 시 작성자를 제외한 활성 구성원에게 알림을 요청한다")
	fun handle_scheduleCreated_excludesCreatorFromNotificationRequests() {
		val recorder = RecordingNotificationPublishInPort()
		val listener = listener(
			members = listOf(
				member("creator", "유니콘"),
				member("family", "가족"),
				member("inactive", "비활성", active = false),
			),
			recorder = recorder,
		)

		listener.handle(ScheduleCreatedEvent("SC1", "circle-1", "creator", "제주도 여행"))

		assertThat(recorder.commands).extracting<String> { it.receiverMemberId }
			.containsExactly("family")
		assertThat(recorder.commands).allSatisfy { command ->
			assertThat(command.payload.type).isEqualTo(NotificationType.SCHEDULE_CREATED)
			assertThat(command.scheduleId).isEqualTo("SC1")
			assertThat(command.payload.toVariables()).containsEntry("schedule_id", "SC1")
		}
	}

	@Test
	@DisplayName("리마인드는 활성 구성원 모두를 중앙 알림 정책으로 전달한다")
	fun handle_reminder_requestsNotificationForActiveMembers() {
		val recorder = RecordingNotificationPublishInPort()
		val listener = listener(
			listOf(member("enabled", "수신"), member("inactive", "비활성", active = false)),
			recorder,
		)

		listener.handle(ScheduleReminderDueEvent(ScheduleReminderType.D7, "SC1", "circle-1", "여행", null))

		val command = recorder.commands.single()
		assertThat(command.receiverMemberId).isEqualTo("enabled")
		assertThat(command.payload.type).isEqualTo(NotificationType.SCHEDULE_REMINDER_D7)
		assertThat(command.eventId).isEqualTo("d7:SC1")
		assertThat(command.payload.toVariables()).containsEntry("schedule_id", "SC1")
	}

	@Test
	@DisplayName("D-day 리마인더 알림 payload에는 일정 ID를 포함한다")
	fun handle_dDayReminder_includesScheduleIdInPayload() {
		val recorder = RecordingNotificationPublishInPort()
		val listener = listener(listOf(member("receiver", "수신")), recorder)

		listener.handle(ScheduleReminderDueEvent(ScheduleReminderType.DDAY_ALL_DAY, "SC1", "circle-1", "여행", null))
		listener.handle(ScheduleReminderDueEvent(ScheduleReminderType.DDAY_TIMED, "SC2", "circle-1", "회의", java.time.LocalTime.of(9, 0)))

		assertThat(recorder.commands.map { it.payload.toVariables() })
			.allSatisfy { variables -> assertThat(variables).containsKey("schedule_id") }
		assertThat(recorder.commands.map { it.payload.toVariables().getValue("schedule_id") })
			.containsExactly("SC1", "SC2")
	}

	@Test
	@DisplayName("가족 확인 완료 시 일정 작성자에게 알림을 요청한다")
	fun handle_scheduleConfirmed_requestsNotificationForCreator() {
		val recorder = RecordingNotificationPublishInPort()
		val listener = listener(
			listOf(member("creator", "작성자"), member("confirmer", "가족"), member("other", "다른 가족")),
			recorder,
		)

		listener.handle(ScheduleConfirmedEvent("SC1", "circle-1", "creator", "confirmer", "여행"))

		assertThat(recorder.commands).extracting<String> { it.receiverMemberId }
			.containsExactly("creator")
		assertThat(recorder.commands).allSatisfy { command ->
			assertThat(command.payload.type).isEqualTo(NotificationType.SCHEDULE_CONFIRMED_BY_FAMILY)
			assertThat(command.eventId).isEqualTo("SC1:confirmer")
			assertThat(command.payload.toVariables()).containsEntry("schedule_id", "SC1")
		}
	}

	@Test
	@DisplayName("확인 요청은 작성자와 이미 확인한 구성원을 제외한다")
	fun handle_confirmationRequest_excludesCreatorAndConfirmedMembers() {
		val recorder = RecordingNotificationPublishInPort()
		val listener = listener(
			listOf(member("creator", "작성자"), member("unconfirmed", "미확인"), member("confirmed", "확인")),
			recorder,
			confirmedMembers = setOf("confirmed"),
		)

		listener.handle(ScheduleConfirmationRequestDueEvent("SC1", "circle-1", "creator", "여행"))

		assertThat(recorder.commands).extracting<String> { it.receiverMemberId }
			.containsExactly("unconfirmed")
		assertThat(recorder.commands.single().payload.toVariables()).containsEntry("schedule_id", "SC1")
	}

	@Test
	@DisplayName("가족에게 전하기는 발신자를 제외한 활성 구성원에게 알림을 요청한다")
	fun handle_familyNotification_excludesSender() {
		val recorder = RecordingNotificationPublishInPort()
		val listener = listener(
			listOf(member("sender", "보낸사람"), member("receiver", "받는사람")),
			recorder,
		)

		listener.handle(
			FamilyScheduleNotificationRequestedEvent(
				"request-1",
				"SC1",
				"circle-1",
				"sender",
				"여행",
				"D-3",
			),
		)

		val command = recorder.commands.single()
		assertThat(command.receiverMemberId).isEqualTo("receiver")
		assertThat(command.payload.type).isEqualTo(NotificationType.SCHEDULE_FAMILY_NOTIFICATION_REQUESTED)
		assertThat(command.eventId).isEqualTo("request-1")
		assertThat(command.payload.toVariables()).containsEntry("schedule_id", "SC1")
	}

	@Test
	@DisplayName("일정 삭제 알림 payload에는 일정 ID를 포함한다")
	fun handle_scheduleDeleted_includesScheduleIdInPayload() {
		val recorder = RecordingNotificationPublishInPort()
		val listener = listener(listOf(member("deleter", "삭제자"), member("receiver", "수신")), recorder)

		listener.handle(ScheduleDeletedEvent("SC1", "circle-1", "deleter", "여행"))

		assertThat(recorder.commands).allSatisfy { command ->
			assertThat(command.payload.type).isEqualTo(NotificationType.SCHEDULE_DELETED)
			assertThat(command.payload.toVariables()).containsEntry("schedule_id", "SC1")
		}
	}

	private fun listener(
		members: List<CircleMemberDto>,
		recorder: RecordingNotificationPublishInPort,
		confirmedMembers: Set<String> = emptySet(),
	) = ScheduleNotificationEventListener(
		FakeCircleMemberInPort(members),
		FakeScheduleConfirmationStatusInPort(confirmedMembers),
		recorder,
	)

	private fun member(memberId: String, nickname: String, active: Boolean = true) =
		CircleMemberDto(memberId, nickname, "MEMBER", active)

	private class RecordingNotificationPublishInPort : NotificationPublishInPort {
		val commands = mutableListOf<PublishNotificationCommand>()
		override fun publish(command: PublishNotificationCommand) {
			commands += command
		}
	}

	private class FakeScheduleConfirmationStatusInPort(
		private val confirmedMembers: Set<String>,
	) : ScheduleConfirmationStatusInPort {
		override fun hasConfirmed(scheduleId: ScheduleId, memberId: String): Boolean = memberId in confirmedMembers
	}

	private class FakeCircleMemberInPort(
		private val members: List<CircleMemberDto>,
	) : CircleMemberInPort {
		override fun getCircleMembers(circleId: String): List<CircleMemberDto> = members
		override fun join(circleId: String, memberId: String): JoinCircleResult = error("not used")
		override fun leave(circleId: String, memberId: String) = error("not used")
		override fun isCircleMember(circleId: String, memberId: String): Boolean = error("not used")
		override fun transferInitiator(circleId: String, currentInitiatorId: String, newInitiatorId: String): CircleSummary =
			error("not used")
		override fun handleMemberWithdrawal(memberId: String) = error("not used")
	}
}
