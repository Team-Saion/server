package com.unicorn.server.infrastructure.adapter.`in`.web.schedule

import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.TodoProgressRateResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("TodoProgressRateResponse 단위 테스트")
class TodoProgressRateResponseTest {
	@Test
	@DisplayName("할일이 없으면 전체와 내 할일 진행률 모두 0을 반환한다")
	fun from_withNoTodos_returnsZeroRates() {
		val response = TodoProgressRateResponse.from(emptyList())

		assertThat(response).extracting(
			"rate", "doneCount", "totalCount", "myRate", "myDoneCount", "myTotalCount",
		).containsExactly(0, 0, 0, 0, 0, 0)
	}
}
