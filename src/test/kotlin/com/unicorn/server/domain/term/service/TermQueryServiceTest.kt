package com.unicorn.server.domain.term.service

import com.unicorn.server.domain.term.Term
import com.unicorn.server.domain.term.enums.TermCode
import com.unicorn.server.domain.term.port.out.TermOutPort
import com.unicorn.server.domain.term.vo.TermId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/** TermQueryService의 "term_code별 최신 버전 선택" 규칙을 fake port로 검증하는 단위 테스트다. */
@DisplayName("TermQueryService 단위 테스트")
class TermQueryServiceTest {

	private val termOutPort = FakeTermOutPort()
	private val termQueryService = TermQueryService(termOutPort)

	@Test
	@DisplayName("같은 termCode에 여러 버전이 있으면 가장 높은 버전만 반환한다")
	fun getActiveTerms_multipleVersionsOfSameCode_returnsHighestVersionOnly() {
		termOutPort.seed(term(TermCode.SERVICE_USE, version = 1))
		termOutPort.seed(term(TermCode.SERVICE_USE, version = 2))

		val result = termQueryService.getActiveTerms()

		assertThat(result).hasSize(1)
		assertThat(result.first().version).isEqualTo(2)
	}

	@Test
	@DisplayName("termCode가 다르면 각각 최신 버전을 모두 반환한다")
	fun getActiveTerms_differentCodes_returnsLatestVersionPerCode() {
		termOutPort.seed(term(TermCode.SERVICE_USE, version = 1))
		termOutPort.seed(term(TermCode.MARKETING, version = 1))

		val result = termQueryService.getActiveTerms()

		assertThat(result).hasSize(2)
		assertThat(result.map { it.termCode }).containsExactlyInAnyOrder(TermCode.SERVICE_USE, TermCode.MARKETING)
	}

	@Test
	@DisplayName("아직 발효되지 않은 버전은 OutPort가 걸러주므로 서비스는 받은 것을 그대로 그룹핑만 한다")
	fun getActiveTerms_noTerms_returnsEmptyList() {
		val result = termQueryService.getActiveTerms()

		assertThat(result).isEmpty()
	}

	private var nextId = 1L

	private fun term(termCode: TermCode, version: Int): Term = Term.reconstitute(
		id = TermId.of(nextId++),
		termCode = termCode,
		title = "title",
		contentUrl = null,
		version = version,
		required = true,
		effectiveAt = LocalDateTime.now().minusDays(1),
		createdAt = LocalDateTime.now(),
		updatedAt = LocalDateTime.now(),
	)

	/** TermOutPort를 메모리 리스트로 대체하는 fake 구현체다. effectiveAt 필터링만 흉내낸다. */
	private class FakeTermOutPort : TermOutPort {
		private val store = mutableListOf<Term>()

		fun seed(term: Term) {
			store += term
		}

		override fun findAllEffectiveAsOf(now: LocalDateTime): List<Term> =
			store.filter { !it.effectiveAt.isAfter(now) }
	}
}
