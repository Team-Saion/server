package com.unicorn.server.infrastructure.adapter.out.persistence.term

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.term.exception.TermErrorCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.time.LocalDateTime

/**
TermPersistenceAdapter 단위 테스트다.

TermJpaRepository는 프로젝트가 소유한 포트가 아니라 Spring Data JPA가 생성하는
프레임워크 경계 인터페이스라서, fake 대신 Mockito로 "Hibernate가 enum 매핑에
실패했을 때 던지는 IllegalArgumentException"을 흉내낸다.
*/
@DisplayName("TermPersistenceAdapter 단위 테스트")
class TermPersistenceAdapterTest {

	private val termJpaRepository = mock(TermJpaRepository::class.java)
	private val termPersistenceAdapter = TermPersistenceAdapter(termJpaRepository)

	@Test
	@DisplayName("term_code를 TermCode enum으로 매핑할 수 없으면 BusinessException(INVALID_TERM_DATA)로 변환한다")
	fun findAllEffectiveAsOf_unmappableTermCode_throwsBusinessException() {
		val now = LocalDateTime.now()
		`when`(termJpaRepository.findAllByEffectiveAtLessThanEqual(now))
			.thenThrow(IllegalArgumentException("No enum constant TermCode.UNKNOWN"))

		assertThatThrownBy { termPersistenceAdapter.findAllEffectiveAsOf(now) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(TermErrorCode.INVALID_TERM_DATA)
	}
}
