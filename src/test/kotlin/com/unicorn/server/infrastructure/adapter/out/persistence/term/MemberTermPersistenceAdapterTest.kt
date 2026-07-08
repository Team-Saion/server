package com.unicorn.server.infrastructure.adapter.out.persistence.term

import com.unicorn.server.TestIdFactory
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.domain.term.MemberTerm
import com.unicorn.server.domain.term.vo.TermId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("MemberTermPersistenceAdapter 통합 테스트")
class MemberTermPersistenceAdapterTest(
	@param:Autowired private val memberTermPersistenceAdapter: MemberTermPersistenceAdapter,
) {

	@Test
	@DisplayName("회원 약관 동의 내역을 저장하고 도메인으로 복원한다")
	fun saveAll_persistsAndReturnsDomain() {
		val memberId = TestIdFactory.memberId()
		val memberTerm = MemberTerm.create(memberId, TermId.of(1L))

		val result = memberTermPersistenceAdapter.saveAll(listOf(memberTerm))

		assertThat(result).hasSize(1)
		assertThat(result.first().id).isNotNull()
		assertThat(result.first().memberId).isEqualTo(memberId)
		assertThat(result.first().termId).isEqualTo(TermId.of(1L))
		assertThat(result.first().agreedAt).isNotNull()
	}
}
