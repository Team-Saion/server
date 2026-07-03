package com.unicorn.server.domain.member.service

import com.unicorn.server.common.exception.BusinessException
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.exception.MemberErrorCode
import com.unicorn.server.domain.member.exception.MemberNotFoundException
import com.unicorn.server.domain.member.exception.WithdrawnMemberException
import com.unicorn.server.domain.member.port.dto.CompleteOnboardingCommand
import com.unicorn.server.domain.member.port.dto.TokenPair
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.domain.member.port.out.TokenIssuer
import com.unicorn.server.domain.member.port.out.TokenStore
import com.unicorn.server.domain.member.vo.MemberId
import com.unicorn.server.domain.term.MemberTerm
import com.unicorn.server.domain.term.Term
import com.unicorn.server.domain.term.enums.TermCode
import com.unicorn.server.domain.term.exception.TermErrorCode
import com.unicorn.server.domain.term.port.out.MemberTermOutPort
import com.unicorn.server.domain.term.port.out.TermOutPort
import com.unicorn.server.domain.term.vo.TermId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("OnboardingService 단위 테스트")
class OnboardingServiceTest {

	private val memberOutPort = FakeMemberOutPort()
	private val termOutPort = FakeTermOutPort()
	private val memberTermOutPort = FakeMemberTermOutPort()
	private val tokenIssuer = FakeTokenIssuer()
	private val tokenStore = FakeTokenStore()
	private val onboardingService = OnboardingService(memberOutPort, termOutPort, memberTermOutPort, tokenIssuer, tokenStore)

	@Test
	@DisplayName("completeOnboarding 호출 시 닉네임을 저장하고 MEMBER 역할 토큰을 발급한다")
	fun completeOnboarding_success_updatesNicknameRoleAndIssuesTokenPair() {
		val member = memberOutPort.save(Member.create(Email("pending@example.com"), "홍길동", "사용자", Role.PENDING))
		seedRequiredAgreement(member.id)

		val result = onboardingService.completeOnboarding(
			member.id.toString(),
			CompleteOnboardingCommand(" 홍길동 "),
		)

		val savedMember = memberOutPort.findById(member.id)!!
		assertThat(savedMember.nickname).isEqualTo("홍길동")
		assertThat(savedMember.role).isEqualTo(Role.MEMBER)
		assertThat(result.accessToken).isEqualTo("access-${member.id}-MEMBER")
		assertThat(result.refreshToken).isEqualTo("refresh-${member.id}-MEMBER")
		assertThat(tokenStore.findMemberIdByRefreshToken(result.refreshToken)).isEqualTo(member.id.toString())
	}

	@Test
	@DisplayName("잘못된 닉네임으로 completeOnboarding 호출 시 INVALID_NICKNAME 예외가 발생한다")
	fun completeOnboarding_withInvalidNickname_throwsInvalidNickname() {
		val member = memberOutPort.save(Member.create(Email("invalid-onboarding@example.com"), "홍길동", "사용자", Role.PENDING))
		seedRequiredAgreement(member.id)

		assertThatThrownBy {
			onboardingService.completeOnboarding(member.id.toString(), CompleteOnboardingCommand("abc!"))
		}
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(MemberErrorCode.INVALID_NICKNAME)
	}

	@Test
	@DisplayName("필수 약관에 동의하지 않은 멤버로 completeOnboarding 호출 시 REQUIRED_TERMS_NOT_AGREED 예외가 발생한다")
	fun completeOnboarding_withoutRequiredTermAgreement_throwsRequiredTermsNotAgreed() {
		val member = memberOutPort.save(Member.create(Email("terms-onboarding@example.com"), "홍길동", "사용자", Role.PENDING))
		termOutPort.seed(term(id = 1L, termCode = TermCode.SERVICE_USE, version = 1, required = true))

		assertThatThrownBy {
			onboardingService.completeOnboarding(member.id.toString(), CompleteOnboardingCommand("홍길동"))
		}
			.isInstanceOf(BusinessException::class.java)
			.extracting("errorCode")
			.isEqualTo(TermErrorCode.REQUIRED_TERMS_NOT_AGREED)
	}

	@Test
	@DisplayName("존재하지 않는 ID로 completeOnboarding 호출 시 MemberNotFoundException이 발생한다")
	fun completeOnboarding_whenNotFound_throwsMemberNotFoundException() {
		assertThatThrownBy {
			onboardingService.completeOnboarding(MemberId.generate().toString(), CompleteOnboardingCommand("홍길동"))
		}.isInstanceOf(MemberNotFoundException::class.java)
	}

	@Test
	@DisplayName("탈퇴한 멤버로 completeOnboarding 호출 시 WithdrawnMemberException이 발생한다")
	fun completeOnboarding_whenWithdrawn_throwsWithdrawnMemberException() {
		val member = memberOutPort.save(Member.create(Email("withdrawn-onboarding@example.com"), "홍길동", "사용자"))
		member.withdraw()
		memberOutPort.save(member)

		assertThatThrownBy {
			onboardingService.completeOnboarding(member.id.toString(), CompleteOnboardingCommand("홍길동"))
		}.isInstanceOf(WithdrawnMemberException::class.java)
	}

	private fun seedRequiredAgreement(memberId: MemberId) {
		termOutPort.seed(term(id = 1L, termCode = TermCode.SERVICE_USE, version = 1, required = true))
		memberTermOutPort.seed(MemberTerm.create(memberId, TermId.of(1L)))
	}

	private fun term(
		id: Long,
		termCode: TermCode,
		version: Int,
		required: Boolean,
	): Term {
		val now = LocalDateTime.now()
		return Term(
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

	private class FakeMemberOutPort : MemberOutPort {
		private val store = linkedMapOf<MemberId, Member>()

		override fun save(member: Member): Member {
			store[member.id] = member
			return member
		}

		override fun findById(memberId: MemberId): Member? = store[memberId]

		override fun findByEmail(email: Email): Member? =
			store.values.firstOrNull { it.email == email }

		override fun existsByEmail(email: Email): Boolean = findByEmail(email) != null

		override fun findAllDeletedBefore(threshold: LocalDateTime): List<Member> =
			store.values.filter { it.deletedAt != null && it.deletedAt!!.isBefore(threshold) }
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

		fun seed(memberTerm: MemberTerm) {
			store += memberTerm
		}

		override fun saveAll(memberTerms: List<MemberTerm>): List<MemberTerm> {
			store += memberTerms
			return memberTerms
		}

		override fun findAllByMemberId(memberId: MemberId): List<MemberTerm> =
			store.filter { it.memberId == memberId }
	}

	private class FakeTokenIssuer : TokenIssuer {
		override fun issue(memberId: String, role: Role): TokenPair =
			TokenPair("access-$memberId-$role", "refresh-$memberId-$role")

		override fun parseRefreshToken(refreshToken: String): String? = null
	}

	private class FakeTokenStore : TokenStore {
		private val memberToToken = mutableMapOf<String, String>()
		private val tokenToMember = mutableMapOf<String, String>()

		override fun save(memberId: String, refreshToken: String) {
			deleteByMemberId(memberId)
			memberToToken[memberId] = refreshToken
			tokenToMember[refreshToken] = memberId
		}

		override fun findMemberIdByRefreshToken(refreshToken: String): String? =
			tokenToMember[refreshToken]

		override fun deleteByMemberId(memberId: String) {
			memberToToken.remove(memberId)?.let { tokenToMember.remove(it) }
		}
	}
}
