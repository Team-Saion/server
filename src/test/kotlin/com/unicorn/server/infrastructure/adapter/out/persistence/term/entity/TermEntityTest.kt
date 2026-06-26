package com.unicorn.server.infrastructure.adapter.out.persistence.term.entity

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.term.enums.TermCode
import com.unicorn.server.domain.term.exception.TermErrorCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/** TermEntity.toDomain()의 데이터 무결성 검증/예외 변환 동작을 검증하는 단위 테스트다. */
@DisplayName("TermEntity 단위 테스트")
class TermEntityTest {

	@Test
	@DisplayName("영속화되기 전(id가 없는) 엔티티를 도메인으로 변환하면 INVALID_TERM_DATA 예외가 발생한다")
	fun toDomain_beforePersisted_throwsBusinessException() {
		val entity = TermEntity(TermCode.SERVICE_USE, "이용약관", null, 1, "Y", LocalDateTime.now())

		assertThatThrownBy { entity.toDomain() }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(TermErrorCode.INVALID_TERM_DATA)
	}
}
