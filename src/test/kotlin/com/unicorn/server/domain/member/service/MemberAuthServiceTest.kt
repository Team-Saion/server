package com.unicorn.server.domain.member.service

import com.unicorn.server.TestIdFactory
import com.unicorn.server.common.vo.Email
import com.unicorn.server.domain.member.Member
import com.unicorn.server.domain.member.SocialAccount
import com.unicorn.server.domain.member.enums.Role
import com.unicorn.server.domain.member.enums.SocialProvider
import com.unicorn.server.domain.member.exception.DuplicateEmailException
import com.unicorn.server.domain.member.exception.InvalidRefreshTokenException
import com.unicorn.server.domain.member.exception.MemberNotFoundException
import com.unicorn.server.domain.member.exception.WithdrawnMemberException
import com.unicorn.server.domain.member.port.dto.SocialLoginCommand
import com.unicorn.server.domain.member.port.dto.TokenPair
import com.unicorn.server.domain.member.port.out.MemberIdGenerator
import com.unicorn.server.domain.member.port.out.MemberOutPort
import com.unicorn.server.domain.member.port.out.SocialAccountIdGenerator
import com.unicorn.server.domain.member.port.out.SocialAccountOutPort
import com.unicorn.server.domain.member.port.out.TokenIssuer
import com.unicorn.server.domain.member.port.out.TokenStore
import com.unicorn.server.domain.member.vo.MemberId
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@DisplayName("MemberAuthService 단위 테스트")
class MemberAuthServiceTest {

	private val memberOutPort = FakeMemberOutPort()
	private val socialAccountOutPort = FakeSocialAccountOutPort()
	private val tokenIssuer = FakeTokenIssuer()
	private val tokenStore = FakeTokenStore()
	private val memberIdGenerator = object : MemberIdGenerator { override fun next() = TestIdFactory.memberId() }
	private val socialAccountIdGenerator = object : SocialAccountIdGenerator { override fun next() = TestIdFactory.socialAccountId() }
	private val memberAuthService = MemberAuthService(
		memberOutPort,
		socialAccountOutPort,
		memberIdGenerator,
		socialAccountIdGenerator,
		tokenIssuer,
		tokenStore,
	)

	@Test
	@DisplayName("login 호출 시 신규 멤버는 Member와 SocialAccount가 생성되고 토큰이 발급된다")
	fun login_newMember_createsMemberAndSocialAccountAndReturnsTokenPair() {
		val command = SocialLoginCommand(
			provider = SocialProvider.KAKAO,
			providerId = "kakao-123",
			email = "new@example.com",
			name = "신규유저",
			kakaoNickname = "카카오닉네임",
			kakaoProfileImageUrl = "https://example.com/profile.png",
		)

		val result = memberAuthService.login(command)

		assertThat(result.tokenPair.accessToken).isNotBlank()
		assertThat(result.tokenPair.refreshToken).isNotBlank()
		assertThat(result.isNewMember).isTrue()
		assertThat(memberOutPort.existsByEmail(Email("new@example.com"))).isTrue()
		val member = memberOutPort.findByEmail(Email("new@example.com"))!!
		assertThat(member.role).isEqualTo(Role.PENDING)
		val socialAccount = socialAccountOutPort.findByProviderAndProviderId(SocialProvider.KAKAO, "kakao-123")
		assertThat(socialAccount).isNotNull()
		assertThat(socialAccount!!.kakaoNickname).isEqualTo("카카오닉네임")
		assertThat(socialAccount.kakaoProfileImageUrl).isEqualTo("https://example.com/profile.png")
		assertThat(tokenStore.findMemberIdByRefreshToken(result.tokenPair.refreshToken)).isNotNull()
	}

	@Test
	@DisplayName("login 호출 시 카카오 이름이 1자이면 기본 닉네임으로 보정한다")
	fun login_withOneCharacterName_usesDefaultNickname() {
		val command = SocialLoginCommand(
			provider = SocialProvider.KAKAO,
			providerId = "kakao-short-name",
			email = "short@example.com",
			name = "김",
		)

		memberAuthService.login(command)

		val member = memberOutPort.findByEmail(Email("short@example.com"))!!
		assertThat(member.nickname).isEqualTo("사용자")
	}

	@Test
	@DisplayName("login 호출 시 email이 null이어도 이메일 없는 멤버를 생성한다")
	fun login_withNullEmail_createsMemberWithoutEmail() {
		val command = SocialLoginCommand(
			provider = SocialProvider.KAKAO,
			providerId = "kakao-null-email",
			email = null,
			name = "이메일없음",
		)

		val result = memberAuthService.login(command)

		assertThat(result.tokenPair.accessToken).isNotBlank()
		assertThat(result.isNewMember).isTrue()
		val socialAccount = socialAccountOutPort.findByProviderAndProviderId(SocialProvider.KAKAO, "kakao-null-email")!!
		val member = memberOutPort.findById(socialAccount.memberId)!!
		assertThat(member.email).isNull()
	}

	@Test
	@DisplayName("login 호출 시 카카오 이름이 10자 초과이면 닉네임을 10자로 자른다")
	fun login_withTooLongName_truncatesNickname() {
		val longName = "가".repeat(11)
		val command = SocialLoginCommand(
			provider = SocialProvider.KAKAO,
			providerId = "kakao-long-name",
			email = "long@example.com",
			name = longName,
		)

		memberAuthService.login(command)

		val member = memberOutPort.findByEmail(Email("long@example.com"))!!
		assertThat(member.nickname).isEqualTo("가".repeat(10))
	}

	@Test
	@DisplayName("login 호출 시 기존 멤버는 SocialAccount로 조회되고 토큰이 발급된다")
	fun login_existingMember_returnsTokenPair() {
		val member = memberOutPort.save(member("existing@example.com", "홍길동", "길동이"))
		socialAccountOutPort.save(
			SocialAccount.create(
				TestIdFactory.socialAccountId(),
				member.id,
				SocialProvider.KAKAO,
				"kakao-456",
				"existing@example.com",
				"홍길동",
				"https://example.com/existing.png",
			),
		)
		val command = SocialLoginCommand(SocialProvider.KAKAO, "kakao-456", "existing@example.com", "홍길동")

		val result = memberAuthService.login(command)

		assertThat(result.tokenPair.accessToken).isNotBlank()
		assertThat(result.isNewMember).isFalse()
	}

	@Test
	@DisplayName("login 호출 시 기존 소셜 계정의 멤버가 탈퇴 상태이면 WithdrawnMemberException이 발생한다")
	fun login_existingWithdrawnMember_throwsWithdrawnMemberException() {
		val member = memberOutPort.save(member("withdrawn-login@example.com", "홍길동", "길동이"))
		member.withdraw()
		memberOutPort.save(member)
		socialAccountOutPort.save(
			SocialAccount.create(
				TestIdFactory.socialAccountId(),
				member.id,
				SocialProvider.KAKAO,
				"kakao-withdrawn",
				"withdrawn-login@example.com",
				"홍길동",
				"https://example.com/withdrawn.png",
			),
		)
		val command = SocialLoginCommand(SocialProvider.KAKAO, "kakao-withdrawn", "withdrawn-login@example.com", "홍길동")

		assertThatThrownBy { memberAuthService.login(command) }
			.isInstanceOf(WithdrawnMemberException::class.java)
	}

	@Test
	@DisplayName("login 호출 시 다른 providerId로 동일 이메일 가입하면 DuplicateEmailException이 발생한다")
	fun login_duplicateEmail_throwsDuplicateEmailException() {
		memberOutPort.save(member("dup@example.com", "기존유저", "기존"))
		val command = SocialLoginCommand(SocialProvider.KAKAO, "kakao-new-id", "dup@example.com", "신규")

		assertThatThrownBy { memberAuthService.login(command) }
			.isInstanceOf(DuplicateEmailException::class.java)
	}

	@Test
	@DisplayName("logout 호출 시 TokenStore에서 refresh token을 삭제한다")
	fun logout_deletesRefreshTokenFromTokenStore() {
		val member = memberOutPort.save(member("test@example.com", "홍길동", "길동이"))
		tokenStore.save(member.id.toString(), "some-refresh-token")

		memberAuthService.logout(member.id.toString())

		assertThat(tokenStore.findMemberIdByRefreshToken("some-refresh-token")).isNull()
	}

	@Test
	@DisplayName("존재하지 않는 ID로 logout 호출 시 MemberNotFoundException이 발생한다")
	fun logout_whenNotFound_throwsMemberNotFoundException() {
		val unknownId = TestIdFactory.memberId().toString()

		assertThatThrownBy { memberAuthService.logout(unknownId) }
			.isInstanceOf(MemberNotFoundException::class.java)
	}

	@Test
	@DisplayName("유효한 refresh token으로 reissue 호출 시 새 토큰 쌍을 발급하고 기존 토큰을 무효화한다")
	fun reissue_withValidRefreshToken_rotatesTokenPair() {
		val member = memberOutPort.save(member("reissue@example.com", "홍길동", "길동이"))
		val oldRefreshToken = "old-refresh-token"
		tokenIssuer.registerRefreshToken(oldRefreshToken, member.id.toString())
		tokenStore.save(member.id.toString(), oldRefreshToken)

		val result = memberAuthService.reissue(oldRefreshToken)

		assertThat(result.accessToken).isEqualTo("access-${member.id}")
		assertThat(result.refreshToken).isEqualTo("refresh-${member.id}")
		assertThat(tokenStore.findMemberIdByRefreshToken(oldRefreshToken)).isNull()
		assertThat(tokenStore.findMemberIdByRefreshToken(result.refreshToken)).isEqualTo(member.id.toString())
	}

	@Test
	@DisplayName("검증할 수 없는 refresh token으로 reissue 호출 시 InvalidRefreshTokenException이 발생한다")
	fun reissue_withInvalidRefreshToken_throwsInvalidRefreshTokenException() {
		assertThatThrownBy { memberAuthService.reissue("invalid-refresh-token") }
			.isInstanceOf(InvalidRefreshTokenException::class.java)
	}

	@Test
	@DisplayName("저장소의 활성 토큰과 다른 refresh token으로 reissue 호출 시 InvalidRefreshTokenException이 발생한다")
	fun reissue_withInactiveRefreshToken_throwsInvalidRefreshTokenException() {
		val member = memberOutPort.save(member("inactive@example.com", "홍길동", "길동이"))
		val inactiveRefreshToken = "inactive-refresh-token"
		tokenIssuer.registerRefreshToken(inactiveRefreshToken, member.id.toString())

		assertThatThrownBy { memberAuthService.reissue(inactiveRefreshToken) }
			.isInstanceOf(InvalidRefreshTokenException::class.java)
	}

	@Test
	@DisplayName("탈퇴한 멤버의 refresh token으로 reissue 호출 시 WithdrawnMemberException이 발생한다")
	fun reissue_withWithdrawnMember_throwsWithdrawnMemberException() {
		val member = memberOutPort.save(member("withdrawn-reissue@example.com", "홍길동", "길동이"))
		member.withdraw()
		memberOutPort.save(member)
		val refreshToken = "withdrawn-refresh-token"
		tokenIssuer.registerRefreshToken(refreshToken, member.id.toString())
		tokenStore.save(member.id.toString(), refreshToken)

		assertThatThrownBy { memberAuthService.reissue(refreshToken) }
			.isInstanceOf(WithdrawnMemberException::class.java)
	}

	private fun member(email: String, name: String?, nickname: String, role: Role = Role.MEMBER): Member =
		Member.create(TestIdFactory.memberId(), Email(email), name, nickname, role)

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

	private class FakeSocialAccountOutPort : SocialAccountOutPort {
		private val store = linkedMapOf<Pair<SocialProvider, String>, SocialAccount>()

		override fun save(socialAccount: SocialAccount): SocialAccount {
			store[socialAccount.provider to socialAccount.providerId] = socialAccount
			return socialAccount
		}

		override fun findByProviderAndProviderId(provider: SocialProvider, providerId: String): SocialAccount? =
			store[provider to providerId]

		override fun findByMemberId(memberId: MemberId): SocialAccount? =
			store.values.firstOrNull { it.memberId == memberId }
	}

	private class FakeTokenIssuer : TokenIssuer {
		private val refreshTokens = mutableMapOf<String, String>()

		override fun issue(memberId: String, role: Role): TokenPair =
			TokenPair("access-$memberId", "refresh-$memberId")

		override fun parseRefreshToken(refreshToken: String): String? =
			refreshTokens[refreshToken]

		fun registerRefreshToken(refreshToken: String, memberId: String) {
			refreshTokens[refreshToken] = memberId
		}
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
