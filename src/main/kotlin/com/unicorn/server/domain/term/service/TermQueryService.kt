package com.unicorn.server.domain.term.service

import com.unicorn.server.domain.term.Term
import com.unicorn.server.domain.term.port.`in`.GetActiveTermsInPort
import com.unicorn.server.domain.term.port.out.TermOutPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
"term_code별로 effective_at이 지난 version 중 가장 높은 것만 선택한다"는
비즈니스 규칙을 담당하는 유스케이스 서비스다.

- 이 선택 로직을 DB 쿼리(윈도우 함수/서브쿼리)가 아니라 이 계층에서 처리한다.
  TermOutPort를 fake로 구현해서 Spring/DB 없이 단위 테스트할 수 있게 하기 위함이다.
- 약관 row 수가 적은 참조 데이터라서 in-memory 그룹핑으로도 성능 문제가 없다.
- 약관 종류가 크게 늘어나면 DB 쿼리로 옮기는 것을 고려할 수 있지만, 현재 범위에서는
  해당하지 않는다.
*/
@Service
@Transactional(readOnly = true)
class TermQueryService(
	private val termOutPort: TermOutPort,
) : GetActiveTermsInPort {

	override fun getActiveTerms(): List<Term> =
		termOutPort.findAllEffectiveAsOf(LocalDateTime.now())
			.groupBy { it.termCode }
			.mapNotNull { (_, versions) -> versions.maxByOrNull { it.version } }
}
