package com.unicorn.server.domain.schedule.vo

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("TodoId 단위 테스트")
class TodoIdTest {
	@Test @DisplayName("시퀀스로 생성한 ID는 TD, KST 12자리 시각, 5자리 시퀀스 형식이다")
	fun generate_withSequence_returnsSpecifiedFormat() { val id = TodoId.generate(42).value; assertThat(id).matches("TD\\d{12}00042").hasSize(19) }
	@Test @DisplayName("문자열 변환은 원본 값을 유지한다")
	fun of_withValue_roundTripsToString() { assertThat(TodoId.of("TD-custom").toString()).isEqualTo("TD-custom") }
}
