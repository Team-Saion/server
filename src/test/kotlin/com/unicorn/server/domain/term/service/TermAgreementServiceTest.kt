package com.unicorn.server.domain.term.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.domain.term.MemberTerm
import com.unicorn.server.domain.term.Term
import com.unicorn.server.domain.term.enums.TermCode
import com.unicorn.server.domain.term.exception.TermErrorCode
import com.unicorn.server.domain.term.port.dto.AgreeTermsCommand
import com.unicorn.server.domain.term.port.out.MemberTermOutPort
import com.unicorn.server.domain.term.port.out.TermOutPort
import com.unicorn.server.domain.term.vo.TermId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("TermAgreementService 단위 테스트")
class TermAgreementServiceTest {

	private val termOutPort = FakeTermOutPort()
	private val memberTermOutPort = FakeMemberTermOutPort()
	private val termAgreementService = TermAgreementService(termOutPort, memberTermOutPort)

	@Test
	@DisplayName("필수 약관을 모두 동의하면 회원 약관 동의 내역을 저장한다")
	fun agreeTerms_withAllRequiredTerms_savesMemberTerms() {
		termOutPort.seed(term(id = 1L, termCode = TermCode.SERVICE_USE, version = 1, required = true))
		termOutPort.seed(term(id = 2L, termCode = TermCode.PRIVACY_COLLECTION, version = 1, required = true))
		termOutPort.seed(term(id = 3L, termCode = TermCode.MARKETING, version = 1, required = false))
		val command = AgreeTermsCommand(
			memberId = MEMBER_ID,
			termIds = listOf(1L, 2L),
		)

		termAgreementService.agreeTerms(command)

		assertThat(memberTermOutPort.saved).hasSize(2)
		assertThat(memberTermOutPort.saved.map { it.memberId.toString() }).containsOnly(MEMBER_ID)
		assertThat(memberTermOutPort.saved.map { it.termId.value }).containsExactlyInAnyOrder(1L, 2L)
	}

	@Test
	@DisplayName("동일한 termId가 중복 입력되어도 한 번만 저장한다")
	fun agreeTerms_withDuplicateTermIds_savesDistinctMemberTerms() {
		termOutPort.seed(term(id = 1L, termCode = TermCode.SERVICE_USE, version = 1, required = true))
		val command = AgreeTermsCommand(
			memberId = MEMBER_ID,
			termIds = listOf(1L, 1L),
		)

		termAgreementService.agreeTerms(command)

		assertThat(memberTermOutPort.saved).hasSize(1)
		assertThat(memberTermOutPort.saved.map { it.termId.value }).containsExactly(1L)
	}

	@Test
	@DisplayName("이미 동의한 약관은 다시 저장하지 않는다")
	fun agreeTerms_withAlreadyAgreedTerms_savesOnlyNewTerms() {
		termOutPort.seed(term(id = 1L, termCode = TermCode.SERVICE_USE, version = 1, required = true))
		termOutPort.seed(term(id = 2L, termCode = TermCode.PRIVACY_COLLECTION, version = 1, required = true))
		memberTermOutPort.seed(MemberTerm.create(MemberId.of(MEMBER_ID), TermId.of(1L)))
		val command = AgreeTermsCommand(
			memberId = MEMBER_ID,
			termIds = listOf(1L, 2L),
		)

		termAgreementService.agreeTerms(command)

		assertThat(memberTermOutPort.saved.map { it.termId.value }).containsExactly(2L)
	}

	@Test
	@DisplayName("모든 약관이 이미 동의된 상태면 저장을 호출하지 않는다")
	fun agreeTerms_withAllTermsAlreadyAgreed_doesNotSave() {
		termOutPort.seed(term(id = 1L, termCode = TermCode.SERVICE_USE, version = 1, required = true))
		memberTermOutPort.seed(MemberTerm.create(MemberId.of(MEMBER_ID), TermId.of(1L)))
		val command = AgreeTermsCommand(
			memberId = MEMBER_ID,
			termIds = listOf(1L),
		)

		termAgreementService.agreeTerms(command)

		assertThat(memberTermOutPort.saved).isEmpty()
	}

	@Test
	@DisplayName("필수 약관 중 하나라도 동의하지 않으면 BusinessException이 발생한다")
	fun agreeTerms_missingRequiredTerm_throwsBusinessException() {
		termOutPort.seed(term(id = 1L, termCode = TermCode.SERVICE_USE, version = 1, required = true))
		termOutPort.seed(term(id = 2L, termCode = TermCode.PRIVACY_COLLECTION, version = 1, required = true))
		val command = AgreeTermsCommand(
			memberId = MEMBER_ID,
			termIds = listOf(1L),
		)

		assertThatThrownBy { termAgreementService.agreeTerms(command) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(TermErrorCode.REQUIRED_TERMS_NOT_AGREED)
		assertThat(memberTermOutPort.saved).isEmpty()
	}

	@Test
	@DisplayName("활성 약관 목록에 없는 termId가 포함되면 INVALID_TERM_ID 예외가 발생한다")
	fun agreeTerms_withInvalidTermId_throwsInvalidTermId() {
		termOutPort.seed(term(id = 1L, termCode = TermCode.SERVICE_USE, version = 1, required = true))
		termOutPort.seed(term(id = 2L, termCode = TermCode.PRIVACY_COLLECTION, version = 1, required = true))
		val command = AgreeTermsCommand(
			memberId = MEMBER_ID,
			termIds = listOf(1L, 2L, 999L),
		)

		assertThatThrownBy { termAgreementService.agreeTerms(command) }
			.isInstanceOf(BusinessException::class.java)
			.extracting { (it as BusinessException).errorCode }
			.isEqualTo(TermErrorCode.INVALID_TERM_ID)
		assertThat(memberTermOutPort.saved).isEmpty()
	}

	@Test
	@DisplayName("같은 약관 코드에 여러 버전이 있으면 활성 최신 버전의 필수 동의 여부만 검사한다")
	fun agreeTerms_multipleVersions_checksLatestVersionOnly() {
		termOutPort.seed(term(id = 1L, termCode = TermCode.SERVICE_USE, version = 1, required = true))
		termOutPort.seed(term(id = 2L, termCode = TermCode.SERVICE_USE, version = 2, required = false))
		val command = AgreeTermsCommand(
			memberId = MEMBER_ID,
			termIds = emptyList(),
		)

		termAgreementService.agreeTerms(command)

		assertThat(memberTermOutPort.saved).isEmpty()
	}

	private fun term(
		id: Long,
		termCode: TermCode,
		version: Int,
		required: Boolean,
	): Term {
		val now = LocalDateTime.now()
		return Term.reconstitute(
			id = TermId.of(id),
			termCode = termCode,
			title = "title",
			contentUrl = null,
			version = version,
			required = required,
			effectiveAt = now.minusDays(1),
			createdAt = now,
			updatedAt = now,
		)
	}

	private class FakeTermOutPort : TermOutPort {
		private val store = mutableListOf<Term>()

		fun seed(term: Term) {
			store += term
		}

		override fun findAllEffectiveAsOf(now: LocalDateTime): List<Term> =
			store.filter { !it.effectiveAt.isAfter(now) }
	}

	private class FakeMemberTermOutPort : MemberTermOutPort {
		private val store = mutableListOf<MemberTerm>()
		val saved = mutableListOf<MemberTerm>()

		fun seed(memberTerm: MemberTerm) {
			store += memberTerm
		}

		override fun saveAll(memberTerms: List<MemberTerm>): List<MemberTerm> {
			store += memberTerms
			saved += memberTerms
			return memberTerms
		}

		override fun findAllByMemberId(memberId: MemberId): List<MemberTerm> =
			store.filter { it.memberId == memberId }
	}

	companion object {
		private const val MEMBER_ID = "00000000-0000-0000-0000-000000000001"
	}
}
