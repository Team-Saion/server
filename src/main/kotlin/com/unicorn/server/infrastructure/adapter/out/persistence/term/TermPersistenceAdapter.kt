package com.unicorn.server.infrastructure.adapter.out.persistence.term

import com.unicorn.server.common.annotation.PersistenceAdapter
import com.unicorn.server.domain.term.Term
import com.unicorn.server.domain.term.port.out.TermOutPort
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
TermOutPort를 JPA 저장소로 구현하는 영속성 어댑터다.

@PersistenceAdapter는 @Component의 별칭으로, 이 클래스가 도메인 포트의 구현체임을
명시적으로 드러내기 위한 마커다.
*/
@PersistenceAdapter
class TermPersistenceAdapter(
	private val termJpaRepository: TermJpaRepository,
) : TermOutPort {

	@Transactional(readOnly = true)
	override fun findAllEffectiveAsOf(now: LocalDateTime): List<Term> =
		termJpaRepository.findAllByEffectiveAtLessThanEqual(now).map { it.toDomain() }
}
