package com.unicorn.server.infrastructure.adapter.out.persistence.term

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.term.Term
import com.unicorn.server.domain.term.exception.TermErrorCode
import com.unicorn.server.domain.term.port.out.TermOutPort
import com.unicorn.server.infrastructure.adapter.out.persistence.term.entity.toDomain
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
TermOutPort를 JPA 저장소로 구현하는 영속성 어댑터다.

@PersistenceAdapter는 @Component의 별칭으로, 이 클래스가 도메인 포트의 구현체임을
명시적으로 드러내기 위한 마커다. term_code 컬럼이 TermCode enum에 없는 값을 담고 있으면
Hibernate가 row를 읽는 시점에 IllegalArgumentException을 던지는데, 이를 그대로
흘려보내지 않고 BusinessException(INVALID_TERM_DATA)로 변환해 원인을 명확히 알린다.
*/
@PersistenceAdapter
class TermPersistenceAdapter(
	private val termJpaRepository: TermJpaRepository,
) : TermOutPort {

	@Transactional(readOnly = true)
	override fun findAllEffectiveAsOf(now: LocalDateTime): List<Term> {
		val entities = try {
			termJpaRepository.findAllByEffectiveAtLessThanEqual(now)
		} catch (e: RuntimeException) {
			if (!e.isCausedByIllegalArgumentException()) throw e
			throw BusinessException(TermErrorCode.INVALID_TERM_DATA, "term_code를 TermCode enum으로 매핑할 수 없습니다", e)
		}

		return entities.map { it.toDomain() }
	}

	private fun RuntimeException.isCausedByIllegalArgumentException(): Boolean =
		generateSequence<Throwable>(this) { it.cause }
			.any { it is IllegalArgumentException }
}
