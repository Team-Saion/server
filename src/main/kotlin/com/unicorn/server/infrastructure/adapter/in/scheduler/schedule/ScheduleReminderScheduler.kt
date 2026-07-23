package com.unicorn.server.infrastructure.adapter.`in`.scheduler.schedule

import com.unicorn.server.domain.schedule.port.`in`.ScheduleConfirmationRequestInPort
import com.unicorn.server.domain.schedule.port.`in`.ScheduleReminderInPort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class ScheduleReminderScheduler(
	private val scheduleReminderInPort: ScheduleReminderInPort,
	private val scheduleConfirmationRequestInPort: ScheduleConfirmationRequestInPort,
) {
	@Scheduled(cron = "0 0 9 * * *", zone = KST_ID)
	fun dispatchDaily() {
		scheduleReminderInPort.dispatchDaily(LocalDateTime.now(KST))
	}

	@Scheduled(cron = "0 * * * * *", zone = KST_ID)
	fun dispatchTimed() {
		scheduleReminderInPort.dispatchTimed(LocalDateTime.now(KST))
	}

	@Scheduled(cron = "0 * * * * *", zone = KST_ID)
	fun dispatchConfirmationRequest() {
		scheduleConfirmationRequestInPort.dispatchDue(LocalDateTime.now(KST))
	}

	companion object {
		private const val KST_ID = "Asia/Seoul"
		private val KST: ZoneId = ZoneId.of(KST_ID)
	}
}
