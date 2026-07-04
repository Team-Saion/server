package com.unicorn.server.domain.schedule

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("ScheduleConfirmation 도메인 단위 테스트")
class ScheduleConfirmationTest {

	@Test
	@DisplayName("확인하기 생성 시 미저장 ID와 생성자 정보가 세팅된다")
	fun create_setsInitialState() {
		val confirmation = ScheduleConfirmation.create(
			scheduleId = 1,
			memberId = "member-1",
			confirmationType = ConfirmationType.CONFIRMED,
			createdBy = "member-1",
		)

		assertThat(confirmation.id).isZero()
		assertThat(confirmation.confirmationType).isEqualTo(ConfirmationType.CONFIRMED)
		assertThat(confirmation.createdBy).isEqualTo("member-1")
		assertThat(confirmation.updatedBy).isEqualTo("member-1")
	}

	@Test
	@DisplayName("확인하기 종류 변경 시 타입과 수정자가 갱신된다")
	fun changeType_updatesTypeAndModifier() {
		val confirmation = ScheduleConfirmation.create(
			scheduleId = 1,
			memberId = "member-1",
			confirmationType = ConfirmationType.CONFIRMED,
			createdBy = "member-1",
		)

		confirmation.changeType(ConfirmationType.CANNOT_ATTEND, "member-2")

		assertThat(confirmation.confirmationType).isEqualTo(ConfirmationType.CANNOT_ATTEND)
		assertThat(confirmation.updatedBy).isEqualTo("member-2")
	}
}
