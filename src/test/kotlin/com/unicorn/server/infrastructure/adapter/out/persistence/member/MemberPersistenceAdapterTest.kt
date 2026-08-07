package com.unicorn.server.infrastructure.adapter.out.persistence.member

import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.Member
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("MemberPersistenceAdapter 통합 테스트")
class MemberPersistenceAdapterTest(
	@param:Autowired private val memberPersistenceAdapter: MemberPersistenceAdapter,
	@param:Autowired private val memberJpaRepository: MemberJpaRepository,
) {

	@Test
	@DisplayName("서로 다른 소셜 계정이 같은 이메일로 가입해도 DB 제약 위반 없이 저장된다")
	fun save_withDuplicateEmailAcrossDifferentMembers_persistsBothSuccessfully() {
		val email = Email("regression-dup@example.com")
		val first = memberPersistenceAdapter.save(Member.create(email, "회원A", "회원에이"))
		memberJpaRepository.flush()

		val second = memberPersistenceAdapter.save(Member.create(email, "회원B", "회원비"))
		memberJpaRepository.flush()

		assertThat(second.id).isNotEqualTo(first.id)
		assertThat(memberPersistenceAdapter.findById(second.id)?.email).isEqualTo(email)
		assertThat(memberPersistenceAdapter.findById(first.id)?.email).isEqualTo(email)
	}

	@Test
	@DisplayName("같은 이메일의 멤버는 모두 조회하고 일치하는 이메일이 없으면 빈 목록을 반환한다")
	fun findAllByEmail_withDuplicateAndMissingEmails_returnsMatchingMembersAndEmptyList() {
		val sharedEmail = Email("find-all-dup@example.com")
		val first = memberPersistenceAdapter.save(Member.create(sharedEmail, "회원A", "회원에이"))
		val second = memberPersistenceAdapter.save(Member.create(sharedEmail, "회원B", "회원비"))
		memberJpaRepository.flush()

		val members = memberPersistenceAdapter.findAllByEmail(sharedEmail)
		val missingMembers = memberPersistenceAdapter.findAllByEmail(Email("missing@example.com"))

		assertThat(members.map { it.id }).containsExactlyInAnyOrder(first.id, second.id)
		assertThat(missingMembers).isEmpty()
	}
}
