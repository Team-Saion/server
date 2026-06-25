package com.unicorn.server.domain.term.port.out

import com.unicorn.server.domain.term.Term
import java.time.LocalDateTime

/**
약관 도메인이 필요로 하는 저장소 조회 기능을 정의하는 출력 포트다.

도메인/유스케이스 계층은 이 인터페이스에만 의존하고, 실제 구현(JPA)은
infrastructure.adapter.out.persistence.term.TermPersistenceAdapter가 담당한다.
나중에 로그인 단계에서 "회원이 동의한 버전이 최신인지" 확인하는 기능을 추가할 때도
이 포트를 그대로 재사용할 수 있도록 범용적인 조회 형태(findAllEffectiveAsOf)로 설계했다.
*/
interface TermOutPort {
	// 주어진 시각 기준으로 이미 발효된 모든 약관 버전을 조회한다. (term_code별 여러 버전 포함)
	fun findAllEffectiveAsOf(now: LocalDateTime): List<Term>
}
