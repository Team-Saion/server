package com.unicorn.server.infrastructure.adapter.out.persistence.term

import com.unicorn.server.infrastructure.adapter.out.persistence.term.entity.TermEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

/**
term 테이블 접근을 담당하는 Spring Data JPA repository다.

findAllByEffectiveAtLessThanEqual은 derived query로 생성되며, "이미 발효된 모든 버전"을
가져오는 용도다. term_code별 최신 버전 선택은 이 레포지토리가 아니라
TermQueryService(use-case 계층)에서 처리한다.
*/
interface TermJpaRepository : JpaRepository<TermEntity, Long> {
	fun findAllByEffectiveAtLessThanEqual(now: LocalDateTime): List<TermEntity>
}
